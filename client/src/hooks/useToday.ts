"use client";

import { getToday } from "@/lib/api";
import { useRemote } from "./useRemote";

export function useToday() {
  return useRemote(getToday);
}
