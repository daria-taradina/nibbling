"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import { useUser } from "@/components/auth/UserProvider";
import { CloseIcon, LogoutIcon, MenuIcon } from "@/components/icons/NavIcons";
import { NAV_ITEMS } from "@/lib/nav";
import { ThemeToggle } from "./ThemeToggle";
import { UserBadge } from "./UserBadge";

export function Navbar() {
  const { user, logout } = useUser();
  const [open, setOpen] = useState(false);
  const wrapperRef = useRef<HTMLDivElement>(null);

  // Close on outside click or Escape.
  useEffect(() => {
    if (!open) return;
    const onPointerDown = (e: PointerEvent) => {
      if (!wrapperRef.current?.contains(e.target as Node)) setOpen(false);
    };
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") setOpen(false);
    };
    document.addEventListener("pointerdown", onPointerDown);
    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("pointerdown", onPointerDown);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, [open]);

  const menuItem =
    "flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-left text-sm transition-colors " +
    "hover:bg-cream-surface dark:hover:bg-ink-raised";

  return (
    <header className="px-4 pt-4 sm:px-6">
      <div
        ref={wrapperRef}
        className="relative mx-auto flex max-w-7xl items-center justify-between rounded-xl2 border border-cream-border
                   bg-white px-4 py-3 text-ink-bg shadow-sm dark:border-ink-border dark:bg-ink-surface dark:text-cream-bg"
      >
        {/* Left: logo + app name */}
        <Link href="/" className="flex items-center gap-3 rounded-lg">
          <Image src="/logo1.png" alt="" width={44} height={38} priority className="h-auto w-11" />
          <span className="flex flex-col leading-tight">
            <span className="font-display text-xl font-semibold">Nibbling</span>
            <span className="text-xs text-ink-bg/60 dark:text-cream-bg/60">Daily Coding Companion</span>
          </span>
        </Link>

        {/* Right: theme + user live in the bar on desktop only; mobile gets them in the menu */}
        <div className="flex items-center gap-3">
          <div className="hidden items-center gap-4 md:flex">
            <ThemeToggle />
            <UserBadge user={user} className="max-w-[10rem]" />
          </div>

          <button
            type="button"
            onClick={() => setOpen((o) => !o)}
            aria-label={open ? "Close menu" : "Open menu"}
            aria-expanded={open}
            aria-controls="navbar-menu"
            className="flex h-10 w-10 items-center justify-center rounded-lg transition-colors
                       hover:bg-cream-surface dark:hover:bg-ink-raised"
          >
            {open ? <CloseIcon /> : <MenuIcon />}
          </button>
        </div>

        {open && (
          <nav
            id="navbar-menu"
            aria-label="Main menu"
            className="absolute right-0 top-full z-50 mt-2 w-full rounded-xl2 border border-cream-border bg-white p-2
                       shadow-lg dark:border-ink-border dark:bg-ink-surface md:w-64"
          >
            {/* Mobile only: user + theme */}
            <div className="md:hidden">
              <div className="flex items-center justify-between gap-3 px-3 py-2">
                <UserBadge user={user} />
                <ThemeToggle />
              </div>
              <div className="my-2 h-px bg-cream-border dark:bg-ink-border" />
            </div>

            {NAV_ITEMS.map((item) => (
              <Link key={item.href} href={item.href} onClick={() => setOpen(false)} className={menuItem}>
                {item.label}
              </Link>
            ))}

            <div className="my-2 h-px bg-cream-border dark:bg-ink-border" />
            <button type="button" onClick={logout} className={menuItem}>
              <LogoutIcon width={18} height={18} />
              Log out
            </button>
          </nav>
        )}
      </div>
    </header>
  );
}
