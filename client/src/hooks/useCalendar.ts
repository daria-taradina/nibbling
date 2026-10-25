"use client";

import { useCallback } from "react";
import { getCalendar } from "@/lib/api";
import { useRemote } from "./useRemote";

// month: "yyyy-MM", or undefined for the current month.
export function useCalendar(month: string | undefined) {
  const fetcher = useCallback(() => getCalendar(month), [month]);
  return useRemote(fetcher);
}
