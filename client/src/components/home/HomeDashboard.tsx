"use client";

import { DailyChallengeCard } from "@/components/challenge/DailyChallengeCard";
import { useStats } from "@/hooks/useStats";
import { CalendarCard } from "./CalendarCard";
import { CreatureCard } from "./CreatureCard";
import { StreakCard } from "./StreakCard";

// Desktop layout:
//   ┌───────────────────────────────────┬──────────────┐
//   │  Creature + Level (full width)    │              │
//   ├──────────────────┬────────────────┤  Calendar    │
//   │  Today's Quiz    │  Streak        │  (row-span)  │
//   └──────────────────┴────────────────┴──────────────┘
//
// Mobile order: Creature → Quiz → Streak → Calendar
export function HomeDashboard() {
  const { data: stats, error, reload } = useStats();

  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1fr_auto]">
      {/* Row 1: Creature spans full left column */}
      <CreatureCard
        stats={stats}
        error={error}
        onRetry={reload}
        className="order-1 lg:col-start-1 lg:row-start-1"
      />

      {/* Row 2: Quiz + Streak side by side, still in left column */}
      <div className="order-2 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:col-start-1 lg:row-start-2">
        <DailyChallengeCard />
        <StreakCard stats={stats} error={error} onRetry={reload} />
      </div>

      {/* Calendar — spans both rows, right column, natural width */}
      <CalendarCard className="order-3 lg:col-start-2 lg:row-span-2 lg:row-start-1" />
    </div>
  );
}
