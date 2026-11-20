"use client";

import { getStats } from "@/lib/api";
import { useRemote } from "./useRemote";

export function useStats() {
  return useRemote(getStats);
}
