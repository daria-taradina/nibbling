"use client";

import { useState } from "react";
import { DayIcon, dayMark } from "@/components/challenge/tiers";
import { Card, CardError, CardSkeleton } from "@/components/ui/Card";
import { useCalendar } from "@/hooks/useCalendar";
import { monthGrid, monthLabel, shiftMonth } from "@/lib/dates";

const WEEKDAY_INITIALS = ["S", "M", "T", "W", "T", "F", "S"];

const LEGEND = [
  { mark: "FEAST", label: "Feast" },
  { mark: "MEAL", label: "Meal" },
  { mark: "SNACK", label: "Snack" },
  { mark: "MISS", label: "Miss" },
] as const;

const navButton =
  "flex h-8 w-8 items-center justify-center rounded-lg hover:bg-cream-surface disabled:cursor-not-allowed " +
  "disabled:opacity-30 dark:hover:bg-ink-raised";

// Month view: each day shows what the player got that day. Self-fetching, so
// paging months only reloads this card.
export function CalendarCard({ className = "" }: { className?: string }) {
  const [month, setMonth] = useState<string | undefined>(undefined); // undefined = current month
  const { data, error, reload } = useCalendar(month);

  const shown = data?.month;
  const currentMonth = data?.today.slice(0, 7);
  const byDate = new Map(data?.days.map((d) => [d.date, d]));

  const nav = shown && (
    <div className="flex items-center gap-1">
      <button type="button" aria-label="Previous month" className={navButton} onClick={() => setMonth(shiftMonth(shown, -1))}>
        ‹
      </button>
      <button
        type="button"
        aria-label="Next month"
        className={navButton}
        disabled={shown >= (currentMonth ?? shown)}
        onClick={() => setMonth(shiftMonth(shown, 1))}
      >
        ›
      </button>
    </div>
  );

  return (
    <Card title={shown ? monthLabel(shown) : "Calendar"} right={nav} className={className}>
      {error && !data && <CardError message={error} onRetry={reload} />}
      {!data && !error && <CardSkeleton lines={5} />}
      {data && shown && (
        <div className="space-y-4">
          <div className="grid grid-cols-7 gap-y-1 text-center text-xs opacity-70" aria-hidden="true">
            {WEEKDAY_INITIALS.map((d, i) => (
              <span key={i}>{d}</span>
            ))}
          </div>

          <ol className="grid grid-cols-7 gap-y-1 text-center">
            {monthGrid(shown).map((date, i) => {
              if (!date) return <li key={`blank-${i}`} aria-hidden="true" />;
              const mark = dayMark(date, byDate.get(date), data.today, data.firstPlayedOn);
              const isToday = date === data.today;
              return (
                <li
                  key={date}
                  aria-current={isToday ? "date" : undefined}
                  className={`mx-auto flex h-11 w-9 flex-col items-center justify-center rounded-lg text-xs ${
                    isToday ? "bg-nibbling-100 ring-1 ring-nibbling-500 dark:bg-nibbling-900/50" : ""
                  }`}
                >
                  <span className={isToday ? "font-semibold" : "opacity-70"}>{Number(date.slice(8))}</span>
                  <span className="flex h-5 items-center text-sm leading-none">
                    <DayIcon mark={mark} />
                  </span>
                </li>
              );
            })}
          </ol>

          <ul className="flex flex-wrap gap-x-4 gap-y-1 border-t border-cream-border pt-3 text-xs dark:border-ink-border">
            {LEGEND.map((l) => (
              <li key={l.mark} className="flex items-center gap-1">
                <DayIcon mark={l.mark} />
                {l.label}
              </li>
            ))}
          </ul>
        </div>
      )}
    </Card>
  );
}
