package com.nibbling.server.service;

import com.nibbling.server.entity.DailyChallenge;
import com.nibbling.server.entity.DailyChallengeQuestion;
import com.nibbling.server.entity.Question;
import com.nibbling.server.entity.QuestionStatus;
import com.nibbling.server.repository.DailyChallengeQuestionRepository;
import com.nibbling.server.repository.DailyChallengeRepository;
import com.nibbling.server.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Decides which 5 questions make up each day: 2 easy, 2 medium, 1 hard, ordered
// easy -> hard, preferring questions that haven't been used for the longest time.
// Days are created ahead of time (startup + nightly), and ensureFor() also
// creates today's on demand, so a sleeping server never leaves a day empty.
@Service
public class DailyChallengePlanner {

    private static final Logger log = LoggerFactory.getLogger(DailyChallengePlanner.class);

    // Difficulty of each of the 5 positions.
    private static final int[] SLOTS = {1, 1, 2, 2, 3};
    private static final int DAYS_AHEAD = 7;

    private final DailyChallengeRepository challenges;
    private final DailyChallengeQuestionRepository dailyQuestions;
    private final QuestionRepository questions;
    private final PuzzleDay puzzleDay;

    public DailyChallengePlanner(DailyChallengeRepository challenges,
                                 DailyChallengeQuestionRepository dailyQuestions,
                                 QuestionRepository questions,
                                 PuzzleDay puzzleDay) {
        this.challenges = challenges;
        this.dailyQuestions = dailyQuestions;
        this.questions = questions;
        this.puzzleDay = puzzleDay;
    }

    @Transactional
    public DailyChallenge ensureFor(LocalDate date) {
        createIfAbsent(date, lastUsed());
        return challenges.findById(date).orElseThrow();
    }

    @Transactional
    public void ensureWindow() {
        Map<Long, LocalDate> lastUsed = lastUsed();
        LocalDate start = puzzleDay.today();
        for (int i = 0; i < DAYS_AHEAD; i++) {
            createIfAbsent(start.plusDays(i), lastUsed);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        try {
            ensureWindow();
        } catch (RuntimeException e) {
            // e.g. no questions seeded yet. Requests will retry via ensureFor().
            log.warn("Could not plan upcoming daily challenges: {}", e.getMessage());
        }
    }

    // Just after midnight Pacific. (If the server was asleep, startup and
    // ensureFor() cover the gap.)
    @Scheduled(cron = "0 10 0 * * *", zone = "${app.puzzle-zone:America/Los_Angeles}")
    public void nightly() {
        try {
            ensureWindow();
        } catch (RuntimeException e) {
            log.warn("Nightly planning failed: {}", e.getMessage());
        }
    }

    private void createIfAbsent(LocalDate date, Map<Long, LocalDate> lastUsed) {
        if (challenges.existsById(date)) {
            return;
        }
        // 1 = we created the row; 0 = another request got there first.
        if (challenges.insertIfAbsent(date) == 0) {
            return;
        }
        List<Question> pool = new ArrayList<>(questions.findByStatus(QuestionStatus.APPROVED));
        if (pool.size() < SLOTS.length) {
            // Throws -> the transaction rolls back, so no half-made day is left behind.
            throw new IllegalStateException(
                    "Need at least " + SLOTS.length + " APPROVED questions, found " + pool.size());
        }

        DailyChallenge challenge = challenges.getReferenceById(date);
        List<Question> picked = new ArrayList<>();
        for (int position = 1; position <= SLOTS.length; position++) {
            Question q = pick(pool, picked, SLOTS[position - 1], lastUsed);
            picked.add(q);
            lastUsed.put(q.getId(), date);
            dailyQuestions.save(new DailyChallengeQuestion(challenge, position, q));
        }
    }

    // Oldest-used question of the wanted difficulty (random among ties). Falls
    // back to any difficulty if that bucket is exhausted.
    private Question pick(List<Question> pool, List<Question> picked, int difficulty,
                          Map<Long, LocalDate> lastUsed) {
        List<Question> candidates = new ArrayList<>();
        for (Question q : pool) {
            if (!picked.contains(q) && q.getDifficulty() == difficulty) {
                candidates.add(q);
            }
        }
        if (candidates.isEmpty()) {
            for (Question q : pool) {
                if (!picked.contains(q)) {
                    candidates.add(q);
                }
            }
        }
        Collections.shuffle(candidates);
        // min() keeps the first of equal elements, so the shuffle breaks ties.
        return candidates.stream()
                .min(Comparator.comparing((Question q) -> lastUsed.getOrDefault(q.getId(), LocalDate.MIN)))
                .orElseThrow();
    }

    private Map<Long, LocalDate> lastUsed() {
        Map<Long, LocalDate> map = new HashMap<>();
        for (Object[] row : dailyQuestions.lastUsedDates()) {
            map.put((Long) row[0], (LocalDate) row[1]);
        }
        return map;
    }
}
