"use client";

import { useTheme } from "next-themes";
import { MoonIcon, SunIcon } from "@/components/icons/NavIcons";

// The active side is styled purely with `dark:` classes (next-themes sets
// the class on <html> before hydration), so there's no mounted-state dance
// and no hydration mismatch.
const segment =
  "flex h-8 w-8 items-center justify-center rounded-full transition-colors";

export function ThemeToggle() {
  const { setTheme } = useTheme();

  return (
    <div
      role="group"
      aria-label="Color theme"
      className="flex items-center gap-1 rounded-full border border-cream-border bg-cream-surface p-1
                 dark:border-ink-border dark:bg-ink-raised"
    >
      <button
        type="button"
        onClick={() => setTheme("light")}
        aria-label="Light mode"
        className={`${segment} bg-nibbling-500 text-white dark:bg-transparent dark:text-cream-bg/50 dark:hover:text-cream-bg`}
      >
        <SunIcon width={16} height={16} />
      </button>
      <button
        type="button"
        onClick={() => setTheme("dark")}
        aria-label="Dark mode"
        className={`${segment} text-ink-bg/50 hover:text-ink-bg dark:bg-nibbling-600 dark:text-white dark:hover:text-white`}
      >
        <MoonIcon width={16} height={16} />
      </button>
    </div>
  );
}
