import type { Metadata } from "next";
import { QuizScreen } from "@/components/challenge/QuizScreen";

export const metadata: Metadata = { title: "Today's quiz · Nibbling" };

export default function QuizPage() {
  return <QuizScreen />;
}
