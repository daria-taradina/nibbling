"use client";

import { DailyChallengeCard } from "@/components/challenge/DailyChallengeCard";
import { useStats } from "@/hooks/useStats";
import { CalendarCard } from "./CalendarCard";
import { StreakCard } from "./StreakCard";
import { XpCard } from "./XpCard";

// Stand-in for cards we haven't built yet (companion, quote).
function Slot({ title, className = "" }: { title: string; className?: string }) {
  return (
    <section
      className={`flex min-h-40 items-center justify-center rounded-xl2 border border-dashed border-cream-border
                  bg-white/60 text-sm text-ink-bg/50 dark:border-ink-border dark:bg-ink-surface/60 dark:text-cream-bg/50
                  ${className}`}
    >
      {title}
    </section>
  );
}

// Home page grid, laid out like the mockup. DOM order = mobile order; on large
// screens the calendar is pinned to the top-right and spans two rows.
export function HomeDashboard() {
  // XP and streak share one request, so fetch once here.
  const { data: stats, error, reload } = useStats();

  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-12">
      <XpCard stats={stats} error={error} onRetry={reload} className="min-h-64 lg:col-span-4" />
      <StreakCard stats={stats} error={error} onRetry={reload} className="lg:col-span-5" />
      <DailyChallengeCard className="min-h-72 lg:col-span-5" />
      <div className="flex flex-col gap-4 lg:col-span-4">
        <Slot title="Your companion" className="flex-1" />
        <Slot title="Quote" className="flex-1" />
      </div>
      <CalendarCard className="min-h-72 lg:col-span-3 lg:col-start-10 lg:row-span-2 lg:row-start-1" />
    </div>
  );
}
