import { ReactNode } from "react";

// Shared look for the home page cards.
export function Card({
  title,
  icon,
  right,
  children,
  className = "",
}: {
  title: string;
  icon?: ReactNode;
  right?: ReactNode;
  children: ReactNode;
  className?: string;
}) {
  return (
    <section
      className={`rounded-xl2 border border-cream-border bg-white p-5 text-ink-bg dark:border-ink-border dark:bg-ink-surface dark:text-cream-bg ${className}`}
    >
      <header className="mb-4 flex items-center justify-between gap-3">
        <h2 className="flex items-center gap-2 text-lg font-semibold">
          {icon}
          {title}
        </h2>
        {right}
      </header>
      {children}
    </section>
  );
}

export function CardSkeleton({ lines = 3 }: { lines?: number }) {
  return (
    <div className="animate-pulse space-y-3" aria-busy="true" aria-label="Loading">
      {Array.from({ length: lines }, (_, i) => (
        <div key={i} className="h-4 rounded bg-cream-surface dark:bg-ink-raised" style={{ width: `${90 - i * 18}%` }} />
      ))}
    </div>
  );
}

export function CardError({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <div className="space-y-3 text-sm">
      <p className="text-red-600 dark:text-red-400">{message}</p>
      <button
        type="button"
        onClick={onRetry}
        className="rounded-lg border border-cream-border px-3 py-1.5 hover:bg-cream-surface dark:border-ink-border dark:hover:bg-ink-raised"
      >
        Try again
      </button>
    </div>
  );
}
