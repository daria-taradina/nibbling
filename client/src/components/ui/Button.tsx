import { ButtonHTMLAttributes } from "react";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "oauth";
  loading?: boolean;
}

export function Button({ variant = "primary", loading, disabled, className = "", children, ...props }: ButtonProps) {
  const base =
    "inline-flex w-full items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-[15px] font-medium " +
    "transition-colors disabled:cursor-not-allowed disabled:opacity-60";

  const variants = {
    primary: "bg-nibbling-500 text-white hover:bg-nibbling-700 dark:bg-nibbling-600 dark:hover:bg-nibbling-500",
    oauth:
      "border border-cream-border bg-white text-ink-bg hover:bg-cream-surface " +
      "dark:border-ink-border dark:bg-ink-surface dark:text-cream-bg dark:hover:bg-ink-raised",
  };

  return (
    <button className={`${base} ${variants[variant]} ${className}`} disabled={disabled || loading} {...props}>
      {loading ? "Loading…" : children}
    </button>
  );
}