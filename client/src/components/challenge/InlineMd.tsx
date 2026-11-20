// Questions are written in markdown, but we only need `inline code` for now.
// (Swap in a markdown library later if questions need lists or bold.)
export function InlineMd({ text }: { text: string }) {
  const parts = text.split(/(`[^`]+`)/g);
  return (
    <>
      {parts.map((part, i) =>
        part.length > 2 && part.startsWith("`") && part.endsWith("`") ? (
          <code
            key={i}
            className="rounded bg-cream-surface px-1 py-0.5 font-mono text-[0.9em] dark:bg-ink-raised"
          >
            {part.slice(1, -1)}
          </code>
        ) : (
          <span key={i}>{part}</span>
        ),
      )}
    </>
  );
}
