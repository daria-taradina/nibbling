import { QuizQuestion, QuizReview } from "@/lib/api";
import { DIFFICULTY_LABEL } from "./tiers";
import { InlineMd } from "./InlineMd";

function Header({ question, badge }: { question: QuizQuestion; badge?: React.ReactNode }) {
  return (
    <div className="mb-3 flex flex-wrap items-center gap-2 text-xs">
      <span className="font-display text-sm font-semibold">Question {question.position}</span>
      <span className="rounded-full bg-cream-surface px-2 py-0.5 dark:bg-ink-raised">{question.topic}</span>
      <span className="rounded-full bg-nibbling-100 px-2 py-0.5 text-nibbling-800 dark:bg-nibbling-800 dark:text-nibbling-100">
        {DIFFICULTY_LABEL[question.difficulty] ?? "Easy"}
      </span>
      {badge && <span className="ml-auto">{badge}</span>}
    </div>
  );
}

function Body({ question }: { question: QuizQuestion }) {
  return (
    <>
      <p className="whitespace-pre-wrap">
        <InlineMd text={question.promptMd} />
      </p>
      {question.codeSnippet && (
        <pre className="mt-3 overflow-x-auto rounded-xl bg-cream-surface p-4 font-mono text-sm dark:bg-ink-raised">
          <code>{question.codeSnippet}</code>
        </pre>
      )}
    </>
  );
}

const cardClass =
  "rounded-xl2 border border-cream-border bg-white p-5 text-ink-bg dark:border-ink-border dark:bg-ink-surface dark:text-cream-bg";

// Answering mode: one radio group per question.
export function QuestionCard({
  question,
  selected,
  onSelect,
  disabled,
}: {
  question: QuizQuestion;
  selected: string[];
  onSelect: (questionId: number, key: string) => void;
  disabled?: boolean;
}) {
  return (
    <section className={cardClass} aria-labelledby={`q-${question.id}-title`}>
      <Header question={question} />
      <div id={`q-${question.id}-title`}>
        <Body question={question} />
      </div>
      <div role="radiogroup" className="mt-4 space-y-2">
        {question.options.map((o) => (
          <label
            key={o.key}
            className="flex cursor-pointer items-start gap-3 rounded-xl border border-cream-border px-4 py-3 transition-colors
                       hover:bg-cream-surface has-[:checked]:border-nibbling-500 has-[:checked]:bg-nibbling-50
                       dark:border-ink-border dark:hover:bg-ink-raised dark:has-[:checked]:bg-nibbling-900/40"
          >
            <input
              type="radio"
              name={`q-${question.id}`}
              value={o.key}
              checked={selected.includes(o.key)}
              disabled={disabled}
              onChange={() => onSelect(question.id, o.key)}
              className="mt-1 accent-nibbling-500"
            />
            <span className="min-w-0 flex-1">
              <span className="mr-2 font-mono text-xs uppercase opacity-50">{o.key}</span>
              <InlineMd text={o.label} />
            </span>
          </label>
        ))}
      </div>
    </section>
  );
}

// Result mode: shows the right answer, what they picked, and why.
export function ReviewCard({ question, review }: { question: QuizQuestion; review: QuizReview }) {
  const unanswered = review.selectedKeys.length === 0;
  const badge = review.correct ? (
    <span className="font-medium text-nibbling-700 dark:text-nibbling-300">✓ Correct</span>
  ) : unanswered ? (
    <span className="font-medium text-tier-miss">Not answered</span>
  ) : (
    <span className="font-medium text-red-600 dark:text-red-400">✗ Incorrect</span>
  );

  return (
    <section className={cardClass}>
      <Header question={question} badge={badge} />
      <Body question={question} />
      <ul className="mt-4 space-y-2">
        {question.options.map((o) => {
          const isCorrect = review.correctKeys.includes(o.key);
          const isPicked = review.selectedKeys.includes(o.key);
          const style = isCorrect
            ? "border-nibbling-500 bg-nibbling-50 dark:bg-nibbling-900/40"
            : isPicked
              ? "border-red-500 bg-red-50 dark:bg-red-950/30"
              : "border-cream-border dark:border-ink-border";
          return (
            <li key={o.key} className={`flex items-start gap-3 rounded-xl border px-4 py-3 ${style}`}>
              <span className="min-w-0 flex-1">
                <span className="mr-2 font-mono text-xs uppercase opacity-50">{o.key}</span>
                <InlineMd text={o.label} />
              </span>
              {isCorrect && (
                <span className="shrink-0 text-xs font-medium text-nibbling-700 dark:text-nibbling-300">
                  {isPicked ? "Your answer ✓" : "Correct answer"}
                </span>
              )}
              {isPicked && !isCorrect && (
                <span className="shrink-0 text-xs font-medium text-red-600 dark:text-red-400">Your answer</span>
              )}
            </li>
          );
        })}
      </ul>
      <p className="mt-4 whitespace-pre-wrap rounded-xl bg-cream-surface p-4 text-sm dark:bg-ink-raised">
        <span className="font-medium">Why: </span>
        <InlineMd text={review.explanationMd} />
      </p>
    </section>
  );
}
