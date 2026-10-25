"use client";

import Link from "next/link";
import { useCallback, useEffect, useRef, useState } from "react";
import { useUser } from "@/components/auth/UserProvider";
import { Button } from "@/components/ui/Button";
import { useToday } from "@/hooks/useToday";
import { AnswersMap, ApiError, saveDraft, startToday, submitToday, TodayChallenge } from "@/lib/api";
import { formatLongDate } from "@/lib/dates";
import { Countdown } from "./Countdown";
import { QuestionCard, ReviewCard } from "./QuestionCard";
import { TIERS } from "./tiers";

const panel =
  "rounded-xl2 border border-cream-border bg-white p-5 text-ink-bg dark:border-ink-border dark:bg-ink-surface dark:text-cream-bg";

// The whole /quiz page. Three states, driven by the backend:
//   NOT_STARTED -> intro + Start      IN_PROGRESS -> all questions, autosaved draft
//   SUBMITTED   -> result + review of every question
export function QuizScreen() {
  const { data, setData, error, reload, loading } = useToday();

  return (
    <div className="mx-auto max-w-2xl space-y-4">
      {loading && <p className={panel}>Loading today&apos;s quiz…</p>}

      {error && !data && (
        <div className={panel}>
          <p className="text-red-600 dark:text-red-400">{error}</p>
          <Button className="mt-3" onClick={reload}>
            Try again
          </Button>
        </div>
      )}

      {data?.state === "NOT_STARTED" && <QuizIntro challenge={data} onStarted={setData} onExpire={reload} />}
      {data?.state === "IN_PROGRESS" && <QuizForm challenge={data} onResult={setData} onReload={reload} />}
      {data?.state === "SUBMITTED" && <QuizResultView challenge={data} onExpire={reload} />}
    </div>
  );
}

function QuizIntro({
  challenge,
  onStarted,
  onExpire,
}: {
  challenge: TodayChallenge;
  onStarted: (c: TodayChallenge) => void;
  onExpire: () => void;
}) {
  const { logout } = useUser();
  const [starting, setStarting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function start() {
    setStarting(true);
    setError(null);
    try {
      onStarted(await startToday());
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) return logout();
      setError(err instanceof Error ? err.message : "Could not start the quiz.");
    } finally {
      setStarting(false);
    }
  }

  return (
    <section className={panel}>
      <p className="text-sm opacity-70">{formatLongDate(challenge.puzzleDate)}</p>
      <h1 className="mt-1 text-2xl font-semibold">Today&apos;s quiz</h1>
      <p className="mt-3">
        {challenge.questionCount} questions. Take your time: your answers save as you go, so you can leave and come
        back. Submit when you&apos;re ready. At the end of the day (Pacific time) we submit whatever you have saved.
      </p>
      <p className="mt-3 text-sm">
        Ends in <Countdown deadlineAt={challenge.deadlineAt} onExpire={onExpire} />
      </p>
      {error && <p className="mt-3 text-sm text-red-600 dark:text-red-400">{error}</p>}
      <Button className="mt-5" onClick={start} loading={starting}>
        Start quiz
      </Button>
    </section>
  );
}

function QuizForm({
  challenge,
  onResult,
  onReload,
}: {
  challenge: TodayChallenge;
  onResult: (c: TodayChallenge) => void;
  onReload: () => void;
}) {
  const { logout } = useUser();
  const [answers, setAnswers] = useState<AnswersMap>(challenge.answers);
  const [saveState, setSaveState] = useState<"saved" | "saving" | "error">("saved");
  const [confirming, setConfirming] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // `latest` is what the next save sends; `dirty` = there are unsaved changes;
  // `chain` makes saves run one after another so an old save can never land after a newer one.
  const latest = useRef<AnswersMap>(challenge.answers);
  const dirty = useRef(false);
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const chain = useRef<Promise<void>>(Promise.resolve());

  // 401 -> sign out. 409 -> the quiz changed under us (already submitted), so reload.
  const handled = useCallback(
    (err: unknown): boolean => {
      if (err instanceof ApiError && err.status === 401) {
        logout();
        return true;
      }
      if (err instanceof ApiError && err.status === 409) {
        onReload();
        return true;
      }
      return false;
    },
    [logout, onReload],
  );

  const flush = useCallback(() => {
    chain.current = chain.current.then(async () => {
      if (!dirty.current) return;
      dirty.current = false;
      try {
        const res = await saveDraft(latest.current);
        if (res.state === "SUBMITTED") {
          onReload(); // the day ended while they were answering
          return;
        }
        if (!dirty.current) setSaveState("saved");
      } catch (err) {
        if (handled(err)) return;
        dirty.current = true;
        setSaveState("error");
      }
    });
    return chain.current;
  }, [handled, onReload]);

  function select(questionId: number, key: string) {
    const next = { ...latest.current, [questionId]: [key] };
    latest.current = next;
    dirty.current = true;
    setAnswers(next);
    setSaveState("saving");
    setConfirming(false);
    if (timer.current) clearTimeout(timer.current);
    timer.current = setTimeout(() => void flush(), 500);
  }

  // Leaving the page with an unsaved change: send it anyway.
  useEffect(
    () => () => {
      if (timer.current) clearTimeout(timer.current);
      if (dirty.current) void saveDraft(latest.current, true).catch(() => {});
    },
    [],
  );

  const answered = challenge.questions.filter((q) => (answers[q.id]?.length ?? 0) > 0).length;
  const total = challenge.questions.length;

  async function submit() {
    if (timer.current) clearTimeout(timer.current);
    setSubmitting(true);
    setError(null);
    try {
      await chain.current; // let an in-flight save finish first
      const result = await submitToday(latest.current); // also saves the final answers
      dirty.current = false;
      onResult(result);
    } catch (err) {
      if (handled(err)) return;
      setError(err instanceof Error ? err.message : "Could not submit.");
    } finally {
      setSubmitting(false);
    }
  }

  function onSubmitClick() {
    if (answered < total && !confirming) {
      setConfirming(true);
      return;
    }
    void submit();
  }

  return (
    <>
      <header className={`${panel} flex flex-wrap items-center justify-between gap-3`}>
        <div>
          <h1 className="text-xl font-semibold">Today&apos;s quiz</h1>
          <p className="text-sm opacity-70">{formatLongDate(challenge.puzzleDate)}</p>
        </div>
        <div className="text-right text-sm">
          <p>
            {answered} / {total} answered
          </p>
          <p className="opacity-70">
            Ends in <Countdown deadlineAt={challenge.deadlineAt} onExpire={onReload} />
          </p>
        </div>
      </header>

      {challenge.questions.map((q) => (
        <QuestionCard key={q.id} question={q} selected={answers[q.id] ?? []} onSelect={select} disabled={submitting} />
      ))}

      <section className={panel}>
        <p className="mb-3 text-sm" aria-live="polite">
          {saveState === "saving" && "Saving…"}
          {saveState === "saved" && "All changes saved"}
          {saveState === "error" && (
            <span className="text-red-600 dark:text-red-400">
              Couldn&apos;t save your last change.{" "}
              <button type="button" className="underline" onClick={() => void flush()}>
                Retry
              </button>
            </span>
          )}
        </p>

        {confirming && (
          <p className="mb-3 rounded-xl bg-cream-surface p-3 text-sm dark:bg-ink-raised">
            {total - answered} {total - answered === 1 ? "question is" : "questions are"} unanswered and will count as
            wrong. Submit anyway?
          </p>
        )}
        {error && <p className="mb-3 text-sm text-red-600 dark:text-red-400">{error}</p>}

        <div className="flex gap-3">
          <Button onClick={onSubmitClick} loading={submitting} disabled={answered === 0}>
            {confirming ? "Yes, submit" : "Submit quiz"}
          </Button>
          {confirming && (
            <Button variant="oauth" onClick={() => setConfirming(false)}>
              Keep answering
            </Button>
          )}
        </div>
        {answered === 0 && <p className="mt-2 text-xs opacity-70">Answer at least one question to submit.</p>}
      </section>
    </>
  );
}

function QuizResultView({ challenge, onExpire }: { challenge: TodayChallenge; onExpire: () => void }) {
  const result = challenge.result;
  if (!result) return null; // SUBMITTED always carries a result
  const tier = result.tier ? TIERS[result.tier] : null;
  const byId = new Map(result.review.map((r) => [r.questionId, r]));

  return (
    <>
      <section className={panel}>
        <p className="text-sm opacity-70">{formatLongDate(challenge.puzzleDate)}</p>
        {tier ? (
          <>
            <h1 className="mt-2 flex items-center gap-3 text-2xl font-semibold">
              <span className={`rounded-full border px-4 py-1 ${tier.chip}`}>
                {tier.emoji} {tier.label}
              </span>
            </h1>
            <p className="mt-3">
              {result.correctCount} of {result.total} correct · <span className="font-medium">+{result.xp} XP</span>
            </p>
          </>
        ) : (
          <>
            <h1 className="mt-2 text-2xl font-semibold">Missed today</h1>
            <p className="mt-3">No answers were saved before the day ended, so there&apos;s no result to count.</p>
          </>
        )}
        {result.submitReason === "DEADLINE" && tier && (
          <p className="mt-2 text-sm opacity-70">
            The day ended, so we submitted the answers you had saved.
          </p>
        )}
        <p className="mt-4 text-sm">
          Next quiz in <Countdown deadlineAt={challenge.deadlineAt} onExpire={onExpire} />
        </p>
        <Link
          href="/"
          className="mt-4 inline-block rounded-xl border border-cream-border px-4 py-2 text-sm font-medium hover:bg-cream-surface dark:border-ink-border dark:hover:bg-ink-raised"
        >
          Back to home
        </Link>
      </section>

      <h2 className="px-1 text-lg font-semibold">Review</h2>
      {challenge.questions.map((q) => {
        const review = byId.get(q.id);
        return review ? <ReviewCard key={q.id} question={q} review={review} /> : null;
      })}
    </>
  );
}
