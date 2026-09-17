// PLACEHOLDER icon (a simple chat bubble), so the button has something to show.
// Swap this for Discord's official logo from their brand kit:
// https://discord.com/branding  (follow their usage guidelines)
export function DiscordIcon({ className = "h-5 w-5" }: { className?: string }) {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={2}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M21 12a8 8 0 0 1-11.6 7.1L4 20.5l1.4-4.9A8 8 0 1 1 21 12z" />
      <circle cx="9" cy="12" r="0.75" fill="currentColor" />
      <circle cx="15" cy="12" r="0.75" fill="currentColor" />
    </svg>
  );
}
