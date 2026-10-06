"use client";

import { createContext, useCallback, useContext, useEffect, useState, ReactNode } from "react";
import { useRouter } from "next/navigation";
import { ApiError, getMe, UserSummary } from "@/lib/api";
import { clearToken, getToken } from "@/lib/auth";

interface UserContextValue {
  user: UserSummary;
  logout: () => void;
}

const UserContext = createContext<UserContextValue | null>(null);

export function useUser() {
  const ctx = useContext(UserContext);
  if (!ctx) throw new Error("useUser must be used inside <UserProvider>");
  return ctx;
}

// Gate for everything under (app): loads the signed-in user once, sends
// visitors without a valid token to /login, and renders children only once
// we know who they are (so the navbar never flashes an empty state).
export function UserProvider({ children }: { children: ReactNode }) {
  const router = useRouter();
  const [user, setUser] = useState<UserSummary | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [attempt, setAttempt] = useState(0);

  const logout = useCallback(() => {
    clearToken();
    router.replace("/login");
  }, [router]);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      router.replace("/login");
      return;
    }

    let cancelled = false;
    getMe(token)
      .then((me) => {
        if (!cancelled) setUser(me);
      })
      .catch((err) => {
        if (cancelled) return;
        if (err instanceof ApiError && err.status === 401) {
          // Expired/invalid token or deleted account: start over.
          clearToken();
          router.replace("/login");
        } else {
          // Backend down / network error: don't log them out, let them retry.
          setError("Can't reach Nibbling right now.");
        }
      });

    return () => {
      cancelled = true;
    };
  }, [router, attempt]);

  if (error) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center gap-4 bg-cream-bg px-4 text-center dark:bg-ink-bg">
        <p className="text-ink-bg/70 dark:text-cream-bg/70">{error}</p>
        <button
          type="button"
          onClick={() => {
            setError(null);
            setAttempt((n) => n + 1);
          }}
          className="rounded-xl bg-nibbling-500 px-4 py-2 text-sm font-medium text-white hover:bg-nibbling-700 dark:bg-nibbling-600 dark:hover:bg-nibbling-500"
        >
          Try again
        </button>
      </main>
    );
  }

  if (!user) {
    return <div className="min-h-screen bg-cream-bg dark:bg-ink-bg" aria-busy="true" />;
  }

  return <UserContext.Provider value={{ user, logout }}>{children}</UserContext.Provider>;
}
