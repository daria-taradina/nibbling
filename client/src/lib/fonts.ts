import { Fredoka, Inter, JetBrains_Mono } from "next/font/google";

// Fredoka: rounded, friendly display face — carries the "cute companion"
// personality on headings and buttons without tipping into childish.
export const fredoka = Fredoka({
  subsets: ["latin"],
  weight: ["500", "600", "700"],
  variable: "--font-fredoka",
});

// Inter: quiet, legible body face for forms/copy/nav.
export const inter = Inter({
  subsets: ["latin"],
  weight: ["400", "500", "600"],
  variable: "--font-inter",
});

// JetBrains Mono: for the code editor + solve-time/runtime stats,
// where a monospace face is functional, not decorative.
export const jetbrainsMono = JetBrains_Mono({
  subsets: ["latin"],
  weight: ["400", "500"],
  variable: "--font-jetbrains-mono",
});