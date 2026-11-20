import { StatsResponse } from "@/lib/api";

// One image per mood per form lives at /public/creature/<form>/<mood>.webp.
// Swapping in new (e.g. hand-drawn) art = replace the files, keep the names.
export type Mood = "neutral" | "happy" | "sad" | "excited" | "sleeping";

export type CreatureForm = "caterpillar";

// Level -> form. Only the caterpillar art exists so far; when the second form
// is drawn, add it to CreatureForm and return it from here for the higher levels.
export function formForLevel(level: number): CreatureForm {
  void level;
  return "caterpillar";
}

export function creatureSrc(form: CreatureForm, mood: Mood) {
  return `/creature/${form}/${mood}.webp`;
}

// What the creature feels on the home page, worked out from /stats alone.
//   happy    today's quiz is done
//   sad      the streak is broken: they've played before today, have no streak,
//            and haven't finished today yet (finishing flips it to happy)
//   neutral  everything else: brand new, or the streak is alive and today is still open
// "excited" (share card) and "sleeping" are not derived here; pass them to
// <Creature mood=...> directly where they make sense.
export function moodFor(stats: StatsResponse): Mood {
  const todayDone = stats.week.some((d) => d.date === stats.today && d.status === "SUBMITTED" && d.tier !== null);
  if (todayDone) return "happy";

  // firstPlayedOn is set as soon as a quiz is started, so compare to today:
  // someone who first played today has had no chance to break a streak yet.
  const playedBeforeToday = stats.firstPlayedOn !== null && stats.firstPlayedOn < stats.today;
  if (playedBeforeToday && stats.currentStreak === 0) return "sad";

  return "neutral";
}
