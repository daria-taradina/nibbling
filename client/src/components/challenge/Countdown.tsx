"use client";

import { useEffect, useState } from "react";

function pad(n: number) {
  return String(n).padStart(2, "0");
}

export function formatRemaining(ms: number): string {
  const total = Math.floor(ms / 1000);
  return `${pad(Math.floor(total / 3600))}:${pad(Math.floor((total % 3600) / 60))}:${pad(total % 60)}`;
}

// Counts down to an ISO instant. At zero, onExpire fires and then repeats every 5s
// until the parent supplies a new deadline (the next day's), so a slightly fast
// device clock can't leave it stuck on 00:00:00.
export function Countdown({
  deadlineAt,
  onExpire,
  className = "",
}: {
  deadlineAt: string;
  onExpire?: () => void;
  className?: string;
}) {
  const deadline = new Date(deadlineAt).getTime();
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    const id = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(id);
  }, []);

  const remaining = Math.max(0, deadline - now);

  useEffect(() => {
    if (remaining > 0 || !onExpire) return;
    onExpire();
    const id = setInterval(onExpire, 5000);
    return () => clearInterval(id);
  }, [remaining, onExpire]);

  return <span className={`font-mono tabular-nums ${className}`}>{formatRemaining(remaining)}</span>;
}
