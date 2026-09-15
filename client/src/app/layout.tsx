import type { Metadata } from "next";
import { ThemeProvider } from "next-themes";
import { fredoka, inter, jetbrainsMono } from "@/lib/fonts";
import "./globals.css";

export const metadata: Metadata = {
  title: "Nibbling — Daily Coding Companion",
  description: "A daily, bite-sized coding puzzle with a companion that grows as you solve.",
  icons: {
    icon: "/logo1.png",
    apple: "/logo1.png",
  },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html
      lang="en"
      className={`${fredoka.variable} ${inter.variable} ${jetbrainsMono.variable}`}
      suppressHydrationWarning
    >
      <body>
        {/* attribute="class" flips the `.dark` class on <html>, which
            tailwind.config's darkMode:"class" + globals.css key off of */}
        <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
          {children}
        </ThemeProvider>
      </body>
    </html>
  );
}