import { Card, CardError, CardSkeleton } from "@/components/ui/Card";
import { StatsResponse } from "@/lib/api";

// Level names; the last one repeats for any higher level.
const LEVEL_TITLES = ["Beginner", "Learner", "Coder", "Hacker", "Architect"];

export function levelTitle(level: number) {
  return LEVEL_TITLES[Math.min(level, LEVEL_TITLES.length) - 1];
}

export function XpCard({
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
  const percent = stats ? Math.round((stats.xpIntoLevel / stats.xpPerLevel) * 100) : 0;

  return (
    <Card title="Your level" icon={<span aria-hidden="true">🌱</span>} className={className}>
      {error && !stats && <CardError message={error} onRetry={onRetry} />}
      {!stats && !error && <CardSkeleton lines={3} />}
      {stats && (
        <div className="space-y-4">
          <div className="flex items-center gap-4">
            <span
              className="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-nibbling-100 font-display text-3xl font-semibold
                         text-nibbling-700 ring-2 ring-nibbling-300 dark:bg-nibbling-800 dark:text-nibbling-100 dark:ring-nibbling-600"
              aria-hidden="true"
            >
              {stats.level}
            </span>
            <div>
              <p className="font-display text-xl font-semibold">
                Level {stats.level} <span className="font-normal opacity-70">{levelTitle(stats.level)}</span>
              </p>
              <p className="text-sm opacity-70">{stats.totalXp} XP earned in total</p>
            </div>
          </div>

          <div>
            <div
              role="progressbar"
              aria-label="Progress to next level"
              aria-valuemin={0}
              aria-valuemax={stats.xpPerLevel}
              aria-valuenow={stats.xpIntoLevel}
              className="h-3 overflow-hidden rounded-full bg-cream-surface dark:bg-ink-raised"
            >
              <div className="h-full rounded-full bg-nibbling-500 transition-all" style={{ width: `${percent}%` }} />
            </div>
            <p className="mt-2 text-sm">
              {stats.xpIntoLevel} / {stats.xpPerLevel} XP to level {stats.level + 1}
            </p>
          </div>
        </div>
      )}
    </Card>
  );
}
