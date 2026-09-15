import Image from "next/image";
import Link from "next/link";
import { ReactNode } from "react";

interface AuthCardProps {
  title: string;
  subtitle: string;
  children: ReactNode;
  footerText: string;
  footerLinkText: string;
  footerLinkHref: string;
}

export function AuthCard({ title, subtitle, children, footerText, footerLinkText, footerLinkHref }: AuthCardProps) {
  return (
    <main className="flex min-h-screen items-center justify-center bg-cream-bg px-4 py-12 dark:bg-ink-bg">
      <div className="w-full max-w-sm">
        <div className="mb-8 flex flex-col items-center gap-2 text-center">
        <Image src="/logo1.png" alt="Nibbling logo" width={167} height={167} priority />
          <h1 className="text-2xl font-semibold text-ink-bg dark:text-cream-bg">{title}</h1>
          <p className="text-sm text-ink-bg/60 dark:text-cream-bg/60">{subtitle}</p>
        </div>

        <div
          className="rounded-xl2 border border-cream-border bg-white p-6 shadow-sm
                     dark:border-ink-border dark:bg-ink-surface"
        >
          {children}
        </div>

        <p className="mt-6 text-center text-sm text-ink-bg/60 dark:text-cream-bg/60">
          {footerText}{" "}
          <Link href={footerLinkHref} className="font-medium text-nibbling-500 hover:underline dark:text-nibbling-600">
            {footerLinkText}
          </Link>
        </p>
      </div>
    </main>
  );
}
