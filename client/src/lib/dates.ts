// Calendar-day helpers. Everything is a plain "yyyy-MM-dd" string (the backend's
// puzzle dates, in Los Angeles time), and arithmetic happens on calendar days
// only, never on instants, so time zones and DST can't shift a day.

export function parseIso(iso: string): Date {
  const [y, m, d] = iso.split("-").map(Number);
  return new Date(y, m - 1, d);
}

export function toIso(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

export function addDays(iso: string, n: number): string {
  const d = parseIso(iso);
  d.setDate(d.getDate() + n);
  return toIso(d);
}

// Monday..Sunday of the week containing the given day.
export function weekDates(iso: string): string[] {
  const sinceMonday = (parseIso(iso).getDay() + 6) % 7;
  return Array.from({ length: 7 }, (_, i) => addDays(iso, i - sinceMonday));
}

export function formatLongDate(iso: string): string {
  return parseIso(iso).toLocaleDateString(undefined, { weekday: "long", month: "long", day: "numeric" });
}

// "2026-10" -> "October 2026"
export function monthLabel(month: string): string {
  const [y, m] = month.split("-").map(Number);
  return new Date(y, m - 1, 1).toLocaleDateString(undefined, { month: "long", year: "numeric" });
}

export function shiftMonth(month: string, delta: number): string {
  const [y, m] = month.split("-").map(Number);
  const d = new Date(y, m - 1 + delta, 1);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
}

// Cells for a Sunday-first month grid: leading nulls for the empty slots, then every day.
export function monthGrid(month: string): (string | null)[] {
  const [y, m] = month.split("-").map(Number);
  const lead = new Date(y, m - 1, 1).getDay();
  const count = new Date(y, m, 0).getDate();
  return [
    ...Array.from({ length: lead }, () => null),
    ...Array.from({ length: count }, (_, i) => `${month}-${String(i + 1).padStart(2, "0")}`),
  ];
}
