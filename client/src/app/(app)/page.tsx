// Home dashboard scaffold, laid out like the mockup. Each Slot is a stand-in
// for a real component we'll build next (swap the Slot for the component and
// keep the className on a wrapper). DOM order = mobile order.

function Slot({ title, className = "" }: { title: string; className?: string }) {
  return (
    <section
      className={`flex min-h-40 items-center justify-center rounded-xl2 border border-dashed border-cream-border
                  bg-white/60 text-sm text-ink-bg/50 dark:border-ink-border dark:bg-ink-surface/60 dark:text-cream-bg/50
                  ${className}`}
    >
      {title}
    </section>
  );
}

export default function Home() {
  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-12">
      <Slot title="Level + companion hero" className="min-h-64 lg:col-span-4" />
      <Slot title="Today's streak + next reward" className="lg:col-span-5" />
      <Slot title="Today's challenge" className="min-h-72 lg:col-span-5" />
      <div className="flex flex-col gap-4 lg:col-span-4">
        <Slot title="Your companion" className="flex-1" />
        <Slot title="Quote" className="flex-1" />
      </div>
      {/* Last in the DOM for mobile, pinned top-right on desktop */}
      <Slot title="Calendar" className="min-h-72 lg:col-span-3 lg:col-start-10 lg:row-span-2 lg:row-start-1" />
    </div>
  );
}
