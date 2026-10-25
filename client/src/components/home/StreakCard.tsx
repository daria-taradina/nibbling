import { DayIcon, dayMark } from "@/components/challenge/tiers";
import { Card, CardError, CardSkeleton } from "@/components/ui/Card";
import { StatsResponse } from "@/lib/api";
import { weekDates } from "@/lib/dates";

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

  const badge = stats && (
    <span className="rounded-full bg-cream-surface px-3 py-1 text-sm dark:bg-ink-raised">
      <span aria-hidden="true">🔥</span> {stats.currentStreak} day streak
    </span>
  );

  return (
    <Card title="Today's Streak" right={badge} className={className}>
      {error && !stats && <CardError message={error} onRetry={onRetry} />}
      {!stats && !error && <CardSkeleton lines={3} />}
      {stats && (
        <div className="space-y-4">
          <ul className="grid grid-cols-7 gap-1.5 sm:gap-2">
            {dates.map((date, i) => {
              const mark = dayMark(date, byDate.get(date), stats.today, stats.firstPlayedOn);
              const isToday = date === stats.today;
              return (
                <li
                  key={date}
                  className={`flex flex-col items-center gap-1 rounded-xl border px-1 py-2 ${
                    isToday
                      ? "border-nibbling-500 bg-nibbling-50 dark:bg-nibbling-900/40"
                      : "border-cream-border dark:border-ink-border"
                  }`}
                  aria-current={isToday ? "date" : undefined}
                >
                  <span className="flex h-8 items-center justify-center text-2xl">
                    <DayIcon mark={mark} />
                  </span>
                  <span className="text-xs opacity-70">{WEEKDAYS[i]}</span>
                </li>
              );
            })}
          </ul>
          <p className="text-sm opacity-70">
            {stats.currentStreak === 0
              ? "Finish today's quiz to start a streak."
              : `Longest streak: ${stats.longestStreak} ${stats.longestStreak === 1 ? "day" : "days"}`}
          </p>
        </div>
      )}
    </Card>
  );
}
