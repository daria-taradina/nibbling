"use client";

import { useCallback, useEffect, useState } from "react";
import { useUser } from "@/components/auth/UserProvider";
import { ApiError } from "@/lib/api";

// Loads data from the backend. A 401 (expired token) signs the user out.
// `fetcher` must be stable (module-level function or useCallback): changing it
// refetches. While refetching, the previous data stays so nothing flickers.
export function useRemote<T>(fetcher: () => Promise<T>) {
  const { logout } = useUser();
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [nonce, setNonce] = useState(0);

  useEffect(() => {
    let cancelled = false;
    fetcher()
      .then((d) => {
        if (!cancelled) setData(d);
      })
      .catch((err) => {
        if (cancelled) return;
        if (err instanceof ApiError && err.status === 401) {
          logout();
        } else {
          setError(err instanceof Error ? err.message : "Something went wrong.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, [fetcher, nonce, logout]);

  const reload = useCallback(() => {
    setError(null);
    setNonce((n) => n + 1);
  }, []);

  return { data, setData, error, reload, loading: data === null && error === null };
}
