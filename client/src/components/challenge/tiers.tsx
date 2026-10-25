import { DayResult, Tier } from "@/lib/api";

// FEAST / MEAL / SNACK reuse the gold / silver / bronze theme colors.
export const TIERS: Record<Tier, { label: string; emoji: string; chip: string }> = {
  FEAST: { label: "Feast", emoji: "🍱", chip: "border-tier-gold bg-tier-gold/25" },
  MEAL: { label: "Meal", emoji: "🍙", chip: "border-tier-silver bg-tier-silver/25" },
  SNACK: { label: "Snack", emoji: "🍡", chip: "border-tier-bronze bg-tier-bronze/25" },
};

export const DIFFICULTY_LABEL: Record<number, string> = { 1: "Easy", 2: "Medium", 3: "Hard" };

// What to draw for one calendar day.
//   FEAST/MEAL/SNACK  finished with that tier
//   MISS              a day since they started playing with nothing finished
//   IN_PROGRESS       started but not submitted yet
//   OPEN              today, not started
//   NONE              future, or before they ever played (nothing to show)
export type DayMark = Tier | "MISS" | "IN_PROGRESS" | "OPEN" | "NONE";

// Dates are "yyyy-MM-dd" strings, so plain string comparison is chronological.
export function dayMark(
  date: string,
  result: DayResult | undefined,
  today: string,
  firstPlayedOn: string | null,
): DayMark {
  if (result) {
    if (result.status === "IN_PROGRESS") return "IN_PROGRESS";
    return result.tier ?? "MISS";
  }
  if (date === today) return "OPEN";
  if (date < today && firstPlayedOn !== null && date >= firstPlayedOn) return "MISS";
  return "NONE";
}

const MARK_LABEL: Record<DayMark, string> = {
  FEAST: "Feast",
  MEAL: "Meal",
  SNACK: "Snack",
  MISS: "Missed",
  IN_PROGRESS: "In progress",
  OPEN: "Not started",
  NONE: "",
};

export function DayIcon({ mark, className = "" }: { mark: DayMark; className?: string }) {
  const label = MARK_LABEL[mark];
  if (mark === "FEAST" || mark === "MEAL" || mark === "SNACK") {
    return (
      <span role="img" aria-label={label} className={className}>
        {TIERS[mark].emoji}
      </span>
    );
  }
  if (mark === "MISS") {
    return (
      <span role="img" aria-label={label} className={`text-tier-miss ${className}`}>
        ✕
      </span>
    );
  }
  if (mark === "IN_PROGRESS") {
    return (
      <span role="img" aria-label={label} className={`text-nibbling-500 ${className}`}>
        …
      </span>
    );
  }
  if (mark === "OPEN") {
    return (
      <span
        role="img"
        aria-label={label}
        className={`inline-block h-3 w-3 rounded-full border-2 border-nibbling-500 ${className}`}
      />
    );
  }
  return null;
}
