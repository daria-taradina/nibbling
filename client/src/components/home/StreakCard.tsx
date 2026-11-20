"use client";

import { DayIcon, dayMark } from "@/components/challenge/tiers";
import { CardError, CardSkeleton } from "@/components/ui/Card";
import { StatsResponse } from "@/lib/api";
import { weekDates } from "@/lib/dates";
import styles from "./StreakCard.module.css";

const WEEKDAYS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];

export function StreakCard({
  stats,
  error,
  onRetry,
  className = "",
}: {
  stats: StatsResponse | null;
  error: string | null;
  onRetry: () => void;
  className?: string;
}) {
  const dates = stats ? weekDates(stats.today) : [];
  const byDate = new Map(stats?.week.map((d) => [d.date, d]));

  return (
    <section
      className={`rounded-xl2 border border-cream-border bg-white p-4 text-ink-bg
                  dark:border-ink-border dark:bg-ink-surface dark:text-cream-bg ${className}`}
    >
      <header className="mb-3 flex items-center justify-between gap-3">
        <h2 className="font-display text-base font-semibold">Today&apos;s Streak</h2>
        {stats && (
          <span className="rounded-full bg-cream-surface px-2.5 py-0.5 text-xs dark:bg-ink-raised">
            <span aria-hidden="true">🔥</span> {stats.currentStreak} day streak
          </span>
        )}
      </header>

      {error && !stats && <CardError message={error} onRetry={onRetry} />}
      {!stats && !error && <CardSkeleton lines={2} />}

      {stats && (
        <div className="space-y-2.5">
          {/* Bento tray */}
          <ul className={`grid grid-cols-7 ${styles.tray}`}>
            {dates.map((date, i) => {
              const mark = dayMark(date, byDate.get(date), stats.today, stats.firstPlayedOn);
              const isToday = date === stats.today;
              return (
                <li
                  key={date}
                  className={`${styles.cell} ${isToday ? styles.cellToday : ""}`}
                  aria-current={isToday ? "date" : undefined}
                >
                  <span className="flex h-6 items-center justify-center text-lg">
                    <DayIcon mark={mark} />
                  </span>
                  <span className={styles.dayLabel}>{WEEKDAYS[i]}</span>
                </li>
              );
            })}
          </ul>

          <p className="text-xs opacity-60">
            {stats.currentStreak === 0
              ? "Finish today's quiz to start a streak."
              : `Longest streak: ${stats.longestStreak} ${stats.longestStreak === 1 ? "day" : "days"}`}
          </p>
        </div>
      )}
    </section>
  );
}
