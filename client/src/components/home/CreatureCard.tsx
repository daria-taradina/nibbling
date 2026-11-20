import { Creature } from "@/components/creature/Creature";
import { CardError, CardSkeleton } from "@/components/ui/Card";
import { moodFor } from "@/lib/creature";
import { StatsResponse } from "@/lib/api";

// Level names matching the server's LEVEL_THRESHOLDS (0/100/300/600/1000 XP).
// The last one is the cap (Code Master stays at level 5).
const LEVEL_TITLES = ["Hatchling", "Learner", "Coder", "Developer", "Code Master"];

export function levelTitle(level: number) {
  return LEVEL_TITLES[Math.min(level, LEVEL_TITLES.length) - 1];
}

// Creature on top of the forest scene, level + XP underneath. Replaces the old
// XP-only card: same numbers, now with the creature reacting to how you're doing.
export function CreatureCard({
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
    // Fixed card height. The scene is as tall as the card and exactly 2:3 (the art's
    // own ratio), so its width follows from the height and the image is never cropped.
    <section
      className={`flex h-64 flex-row overflow-hidden rounded-xl2 border border-cream-border bg-white text-ink-bg
                  sm:h-72 lg:h-70 dark:border-ink-border dark:bg-ink-surface dark:text-cream-bg ${className}`}
    >
      {/* Scene: whole image, no cropping */}
      <div
        className="relative aspect-[2/3] h-full shrink-0"
        style={{ backgroundImage: "url(/creature/scenes/forest.webp)", backgroundSize: "100% 100%" }}
      >
        {/* Dark overlay in dark mode */}
        <div className="absolute inset-0 bg-ink-bg/0 dark:bg-ink-bg/40" aria-hidden="true" />

        {/* Creature sized relative to the scene, feet on the sandy clearing */}
        <div className="absolute inset-x-0 bottom-[19%] flex justify-center">
          {stats ? <Creature mood={moodFor(stats)} className="w-[82%]" /> : null}
        </div>
      </div>

      {/* Level + XP info on the right */}
      <div className="flex min-w-0 flex-1 flex-col justify-center gap-4 p-5 sm:p-6">
        <h2 className="flex items-center gap-1.5 text-sm font-semibold opacity-60">
          <span aria-hidden="true">🌱</span>
          Your level
        </h2>

        {error && !stats && <CardError message={error} onRetry={onRetry} />}
        {!stats && !error && <CardSkeleton lines={2} />}
        {stats && (
          <>
            <div className="flex items-center gap-3">
              <span
                className="flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-nibbling-100 font-display text-2xl font-semibold
                           text-nibbling-700 ring-2 ring-nibbling-300 dark:bg-nibbling-800 dark:text-nibbling-100 dark:ring-nibbling-600"
                aria-hidden="true"
              >
                {stats.level}
              </span>
              <div>
                <p className="font-display text-lg font-semibold leading-tight sm:text-xl">
                  Level {stats.level} <span className="font-normal opacity-70">{levelTitle(stats.level)}</span>
                </p>
                <p className="text-sm opacity-60">{stats.totalXp} XP total</p>
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
              <p className="mt-2 text-sm opacity-60">
                {stats.xpIntoLevel} / {stats.xpPerLevel} XP to level {stats.level + 1}
              </p>
            </div>
          </>
        )}
      </div>
    </section>
  );
}