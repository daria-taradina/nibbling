import { InputHTMLAttributes, forwardRef } from "react";

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, id, ...props }, ref) => {
    const inputId = id ?? props.name;
    return (
      <div className="flex flex-col gap-1.5">
        <label htmlFor={inputId} className="text-sm font-medium text-ink-bg/80 dark:text-cream-bg/80">
          {label}
        </label>
        <input
          id={inputId}
          ref={ref}
          className="rounded-xl border border-cream-border bg-white px-3.5 py-2.5 text-[15px]
                     text-ink-bg placeholder:text-ink-bg/40 transition-colors
                     focus:border-nibbling-500
                     dark:border-ink-border dark:bg-ink-surface dark:text-cream-bg dark:placeholder:text-cream-bg/30
                     dark:focus:border-nibbling-600"
          aria-invalid={!!error}
          aria-describedby={error ? `${inputId}-error` : undefined}
          {...props}
        />
        {error && (
          <p id={`${inputId}-error`} className="text-sm text-red-600 dark:text-red-400">
            {error}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = "Input";