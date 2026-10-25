package com.nibbling.server.service;

import com.nibbling.server.dto.OptionView;
import com.nibbling.server.dto.QuestionView;
import com.nibbling.server.dto.ResultView;
import com.nibbling.server.dto.ReviewView;
import com.nibbling.server.dto.SaveDraftResponse;
import com.nibbling.server.dto.TodayResponse;
import com.nibbling.server.entity.Attempt;
import com.nibbling.server.entity.AttemptAnswer;
import com.nibbling.server.entity.AttemptStatus;
import com.nibbling.server.entity.DailyChallenge;
import com.nibbling.server.entity.DailyChallengeQuestion;
import com.nibbling.server.entity.Question;
import com.nibbling.server.entity.QuestionOption;
import com.nibbling.server.entity.SubmitReason;
import com.nibbling.server.exception.ChallengeStateException;
import com.nibbling.server.exception.InvalidAnswerException;
import com.nibbling.server.repository.AttemptAnswerRepository;
import com.nibbling.server.repository.AttemptRepository;
import com.nibbling.server.repository.DailyChallengeQuestionRepository;
import com.nibbling.server.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

// The daily quiz flow:
//   getToday  -> NOT_STARTED / IN_PROGRESS / SUBMITTED view (never leaks answers early)
//   start     -> creates today's attempt (idempotent)
//   saveDraft -> replaces the saved answers (pause/resume, any device)
//   submit    -> grades once and freezes the attempt
// An attempt not submitted by the end of the puzzle day is auto-submitted with
// whatever was saved: lazily when the user next shows up, and by AttemptSweeper.
@Service
public class QuizService {

    private static final int MAX_KEYS_PER_QUESTION = 10;

    private final PuzzleDay puzzleDay;
    private final DailyChallengePlanner planner;
    private final AttemptRepository attempts;
    private final AttemptAnswerRepository answers;
    private final DailyChallengeQuestionRepository dailyQuestions;
    private final QuestionRepository questions;
    private final QuizScoring scoring;

    public QuizService(PuzzleDay puzzleDay,
                       DailyChallengePlanner planner,
                       AttemptRepository attempts,
                       AttemptAnswerRepository answers,
                       DailyChallengeQuestionRepository dailyQuestions,
                       QuestionRepository questions,
                       QuizScoring scoring) {
        this.puzzleDay = puzzleDay;
        this.planner = planner;
        this.attempts = attempts;
        this.answers = answers;
        this.dailyQuestions = dailyQuestions;
        this.questions = questions;
        this.scoring = scoring;
    }

    @Transactional
    public TodayResponse getToday(UUID userId) {
        closeOverdueFor(userId);
        LocalDate date = puzzleDay.today();
        DailyChallenge challenge = planner.ensureFor(date);
        Attempt attempt = attempts.findByUserIdAndPuzzleDate(userId, date).orElse(null);
        return view(challenge, attempt);
    }

    @Transactional
    public TodayResponse start(UUID userId) {
        closeOverdueFor(userId);
        LocalDate date = puzzleDay.today();
        DailyChallenge challenge = planner.ensureFor(date);
        attempts.insertIfAbsent(userId, date, puzzleDay.now(), puzzleDay.deadlineFor(date));
        Attempt attempt = attempts.findByUserIdAndPuzzleDate(userId, date).orElseThrow();
        return view(challenge, attempt);
    }

    @Transactional
    public SaveDraftResponse saveDraft(UUID userId, Map<Long, List<String>> draft) {
        Attempt attempt = lockTodaysAttempt(userId);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ChallengeStateException("Today's quiz was already submitted");
        }
        if (!puzzleDay.now().isBefore(attempt.getDeadlineAt())) {
            // The day ended while they were answering: grade what was saved before.
            grade(attempt, SubmitReason.DEADLINE);
            return new SaveDraftResponse(AttemptStatus.SUBMITTED.name(), attempt.getDeadlineAt());
        }
        replaceDraft(attempt, draft);
        return new SaveDraftResponse(AttemptStatus.IN_PROGRESS.name(), attempt.getDeadlineAt());
    }

    @Transactional
    public TodayResponse submit(UUID userId, Map<Long, List<String>> finalAnswers) {
        Attempt attempt = lockTodaysAttempt(userId);
        DailyChallenge challenge = planner.ensureFor(attempt.getPuzzleDate());
        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            return view(challenge, attempt); // double tap: just show the result again
        }
        if (finalAnswers != null) {
            replaceDraft(attempt, finalAnswers);
        }
        if (answers.findByAttemptId(attempt.getId()).isEmpty()) {
            throw new InvalidAnswerException("Answer at least one question before submitting");
        }
        SubmitReason reason = puzzleDay.now().isBefore(attempt.getDeadlineAt())
                ? SubmitReason.USER : SubmitReason.DEADLINE;
        grade(attempt, reason);
        return view(challenge, attempt);
    }

    // Used by AttemptSweeper; each call is its own transaction.
    @Transactional
    public void closeIfOverdue(UUID attemptId) {
        attempts.findForUpdateById(attemptId).ifPresent(a -> {
            if (a.getStatus() == AttemptStatus.IN_PROGRESS && !puzzleDay.now().isBefore(a.getDeadlineAt())) {
                grade(a, SubmitReason.DEADLINE);
            }
        });
    }

    // ---- internals ----

    // Also called by StatsService so yesterday's unfinished attempt is graded
    // (and counted toward XP and streak) before anything is totalled up.
    @Transactional
    public void closeOverdueFor(UUID userId) {
        for (Attempt a : attempts.findByUserIdAndStatusAndDeadlineAtBefore(
                userId, AttemptStatus.IN_PROGRESS, puzzleDay.now())) {
            closeIfOverdue(a.getId());
        }
    }

    private Attempt lockTodaysAttempt(UUID userId) {
        return attempts.findForUpdate(userId, puzzleDay.today())
                .orElseThrow(() -> new ChallengeStateException("Start today's quiz first"));
    }

    private record Loaded(List<DailyChallengeQuestion> slots, Map<Long, Question> byId) {}

    private Loaded load(LocalDate date) {
        List<DailyChallengeQuestion> slots = dailyQuestions.findForDate(date);
        List<Long> ids = slots.stream().map(s -> s.getQuestion().getId()).toList();
        Map<Long, Question> byId = questions.findWithOptionsByIdIn(ids).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));
        return new Loaded(slots, byId);
    }

    private void replaceDraft(Attempt attempt, Map<Long, List<String>> draft) {
        Loaded quiz = load(attempt.getPuzzleDate());
        if (draft.size() > quiz.slots().size()) {
            throw new InvalidAnswerException("Too many answers");
        }
        List<AttemptAnswer> rows = new ArrayList<>();
        for (Map.Entry<Long, List<String>> entry : draft.entrySet()) {
            Question q = quiz.byId().get(entry.getKey());
            if (q == null) {
                throw new InvalidAnswerException("Question " + entry.getKey() + " is not part of today's quiz");
            }
            List<String> keys = entry.getValue() == null ? List.of() : entry.getValue();
            if (keys.size() > MAX_KEYS_PER_QUESTION) {
                throw new InvalidAnswerException("Too many options selected");
            }
            Set<String> valid = q.getOptions().stream()
                    .map(QuestionOption::getOptionKey).collect(Collectors.toSet());
            for (String key : new LinkedHashSet<>(keys)) {
                if (!valid.contains(key)) {
                    throw new InvalidAnswerException("Unknown option for question " + q.getId());
                }
                rows.add(new AttemptAnswer(attempt.getId(), q.getId(), key));
            }
        }
        answers.deleteAllForAttempt(attempt.getId());
        answers.saveAll(rows);
    }

    private void grade(Attempt attempt, SubmitReason reason) {
        Loaded quiz = load(attempt.getPuzzleDate());
        Map<Long, Set<String>> selected = selectedByQuestion(attempt.getId());

        List<QuizScoring.Item> items = new ArrayList<>();
        for (DailyChallengeQuestion slot : quiz.slots()) {
            Question q = quiz.byId().get(slot.getQuestion().getId());
            items.add(new QuizScoring.Item(q.getDifficulty(), isCorrect(q, selected.get(q.getId()))));
        }
        QuizScoring.Outcome outcome = scoring.score(items, !selected.isEmpty());

        // Auto-submitted attempts are stamped with the deadline, not whenever we got around to it.
        Instant at = reason == SubmitReason.DEADLINE ? attempt.getDeadlineAt() : puzzleDay.now();
        attempt.markSubmitted(reason, at, outcome.correctCount(), outcome.total(),
                outcome.score(), outcome.tier(), outcome.xp());
        attempts.save(attempt);
    }

    // Correct only if the selected options are exactly the correct options.
    private static boolean isCorrect(Question q, Set<String> selected) {
        if (selected == null || selected.isEmpty()) {
            return false;
        }
        return selected.equals(correctKeys(q));
    }

    private static Set<String> correctKeys(Question q) {
        return q.getOptions().stream()
                .filter(QuestionOption::isCorrect)
                .map(QuestionOption::getOptionKey)
                .collect(Collectors.toSet());
    }

    private Map<Long, Set<String>> selectedByQuestion(UUID attemptId) {
        return answers.findByAttemptId(attemptId).stream().collect(Collectors.groupingBy(
                AttemptAnswer::getQuestionId,
                Collectors.mapping(AttemptAnswer::getOptionKey, Collectors.toSet())));
    }

    private TodayResponse view(DailyChallenge challenge, Attempt attempt) {
        LocalDate date = challenge.getPuzzleDate();
        String type = challenge.getType().name();

        if (attempt == null) {
            int count = dailyQuestions.findForDate(date).size();
            return new TodayResponse(date, type, "NOT_STARTED", puzzleDay.deadlineFor(date),
                    count, List.of(), Map.of(), null);
        }

        Loaded quiz = load(date);
        Map<Long, Set<String>> selected = selectedByQuestion(attempt.getId());

        List<QuestionView> questionViews = new ArrayList<>();
        for (DailyChallengeQuestion slot : quiz.slots()) {
            Question q = quiz.byId().get(slot.getQuestion().getId());
            List<OptionView> options = q.getOptions().stream()
                    .map(o -> new OptionView(o.getOptionKey(), o.getLabel()))
                    .toList();
            questionViews.add(new QuestionView(q.getId(), slot.getPosition(), q.getTopic(),
                    q.getDifficulty(), q.getKind().name(), q.getPromptMd(), q.getCodeSnippet(), options));
        }

        Map<Long, List<String>> answerMap = new LinkedHashMap<>();
        selected.forEach((qid, keys) -> answerMap.put(qid, new ArrayList<>(new TreeSet<>(keys))));

        ResultView result = null;
        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            List<ReviewView> review = new ArrayList<>();
            for (DailyChallengeQuestion slot : quiz.slots()) {
                Question q = quiz.byId().get(slot.getQuestion().getId());
                Set<String> picked = selected.getOrDefault(q.getId(), Set.of());
                review.add(new ReviewView(q.getId(),
                        new ArrayList<>(new TreeSet<>(picked)),
                        new ArrayList<>(new TreeSet<>(correctKeys(q))),
                        isCorrect(q, picked),
                        q.getExplanationMd()));
            }
            result = new ResultView(attempt.getCorrectCount(), attempt.getTotal(), attempt.getScore(),
                    attempt.getTier() == null ? null : attempt.getTier().name(),
                    attempt.getXp(), attempt.getSubmitReason().name(), review);
        }

        return new TodayResponse(date, type, attempt.getStatus().name(), attempt.getDeadlineAt(),
                questionViews.size(), questionViews, answerMap, result);
    }
}
