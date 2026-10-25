"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useUser } from "@/components/auth/UserProvider";
import { Card, CardError, CardSkeleton } from "@/components/ui/Card";
import { useToday } from "@/hooks/useToday";
import { ApiError, startToday } from "@/lib/api";
import { Countdown } from "./Countdown";
import { TIERS } from "./tiers";

const primaryButton =
  "inline-flex w-full items-center justify-center rounded-xl bg-nibbling-500 px-4 py-2.5 text-[15px] font-medium text-white " +
  "transition-colors hover:bg-nibbling-700 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-nibbling-600 dark:hover:bg-nibbling-500";

const secondaryButton =
  "inline-flex w-full items-center justify-center rounded-xl border border-cream-border px-4 py-2.5 text-[15px] font-medium " +
  "transition-colors hover:bg-cream-surface dark:border-ink-border dark:hover:bg-ink-raised";

// Home page card for today's quiz. The real work happens on /quiz; this just
// shows where the player is and gets them there.
export function DailyChallengeCard({ className = "" }: { className?: string }) {
  const router = useRouter();
  const { logout } = useUser();
  const { data, error, reload, loading } = useToday();
  const [starting, setStarting] = useState(false);
  const [startError, setStartError] = useState<string | null>(null);

  async function start() {
    setStarting(true);
    setStartError(null);
    try {
      await startToday();
      router.push("/quiz");
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) return logout();
      setStartError(err instanceof Error ? err.message : "Could not start the quiz.");
      setStarting(false);
    }
  }

  const countdown = data && (
    <span className="text-sm opacity-70" title="Time left today">
      ⏱ <Countdown deadlineAt={data.deadlineAt} onExpire={reload} />
    </span>
  );

  return (
    <Card title="Today's Quiz" icon={<span aria-hidden="true">🌿</span>} right={countdown} className={className}>
      {loading && <CardSkeleton lines={4} />}
      {error && !data && <CardError message={error} onRetry={reload} />}

      {data?.state === "NOT_STARTED" && (
        <div className="space-y-4">
          <p>
            {data.questionCount} questions to keep your streak going. Your answers save as you go, so you can pause and
            come back any time today.
          </p>
          {startError && <p className="text-sm text-red-600 dark:text-red-400">{startError}</p>}
          <button type="button" className={primaryButton} onClick={start} disabled={starting}>
            {starting ? "Starting…" : "Start quiz"}
          </button>
        </div>
      )}

      {data?.state === "IN_PROGRESS" && (
        <div className="space-y-4">
          <p>
            You&apos;re partway through.{" "}
            <span className="font-medium">
              {Object.values(data.answers).filter((keys) => keys.length > 0).length} of {data.questionCount}
            </span>{" "}
            answered so far.
          </p>
          <Link href="/quiz" className={primaryButton}>
            Continue quiz
          </Link>
        </div>
      )}

      {data?.state === "SUBMITTED" && data.result && (
        <div className="space-y-4">
          {data.result.tier ? (
            <>
              <p className="flex flex-wrap items-center gap-3">
                <span className={`rounded-full border px-3 py-1 font-medium ${TIERS[data.result.tier].chip}`}>
                  {TIERS[data.result.tier].emoji} {TIERS[data.result.tier].label}
                </span>
                <span>
                  {data.result.correctCount} of {data.result.total} correct · +{data.result.xp} XP
                </span>
              </p>
              <p className="text-sm opacity-70">Nice work. A new quiz arrives when the timer hits zero.</p>
            </>
          ) : (
            <p>You missed today&apos;s quiz. A new one arrives when the timer hits zero.</p>
          )}
          <Link href="/quiz" className={secondaryButton}>
            Review answers
          </Link>
        </div>
      )}
    </Card>
  );
}
