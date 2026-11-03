"use client";

import { DailyChallengeCard } from "@/components/challenge/DailyChallengeCard";
import { useStats } from "@/hooks/useStats";
import { CalendarCard } from "./CalendarCard";
import { StreakCard } from "./StreakCard";
import { XpCard } from "./XpCard";

// Home page grid. DOM order = mobile order (XP, Quiz, Streak, Calendar).
// On large screens the calendar pins to the top-right spanning two rows, and
// XP + Quiz stack on the left while Streak sits top-centre.
export function HomeDashboard() {
  const { data: stats, error, reload } = useStats();

  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-12">
      {/* row 1: XP (left) | Streak (centre) | Calendar (right, spans 2 rows) */}
      <XpCard stats={stats} error={error} onRetry={reload} className="min-h-64 lg:col-span-4 lg:row-start-1" />
      <StreakCard stats={stats} error={error} onRetry={reload} className="lg:col-span-5 lg:row-start-1 order-3 lg:order-none" />
      <CalendarCard className="min-h-72 lg:col-span-3 lg:col-start-10 lg:row-span-2 lg:row-start-1 order-4 lg:order-none" />

      {/* row 2: Quiz card spanning XP + Streak columns */}
      <DailyChallengeCard className="min-h-72 lg:col-span-9 lg:row-start-2 order-2 lg:order-none" />
    </div>
  );
}
