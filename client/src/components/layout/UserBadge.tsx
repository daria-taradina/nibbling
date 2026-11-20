import { UserSummary } from "@/lib/api";

// No avatar upload yet, so this is a letter on a green circle. When the
// companion/avatar image exists, swap the inner <span> for an <Image>.
export function Avatar({ username, size = 36 }: { username: string; size?: number }) {
  return (
    <span
      style={{ width: size, height: size }}
      className="flex shrink-0 items-center justify-center rounded-full bg-nibbling-100 font-display text-base font-semibold
                 text-nibbling-700 ring-1 ring-nibbling-200 dark:bg-nibbling-800 dark:text-nibbling-200 dark:ring-nibbling-700"
      aria-hidden="true"
    >
      {username.charAt(0).toUpperCase()}
    </span>
  );
}

export function UserBadge({ user, className = "" }: { user: UserSummary; className?: string }) {
  return (
    <div className={`flex min-w-0 items-center gap-2.5 ${className}`}>
      <Avatar username={user.username} />
      <span className="truncate text-sm font-medium">{user.username}</span>
    </div>
  );
}
