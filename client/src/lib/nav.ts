// Links shown in the navbar's burger menu. Routes only live here, not in
// lib/api.ts (that file is strictly for talking to the Spring Boot backend).
// Add an entry when its page exists, otherwise the link just 404s.
export interface NavItem {
  label: string;
  href: string;
}

export const NAV_ITEMS: NavItem[] = [
  { label: "Home", href: "/" },
  // { label: "Today", href: "/today" },
  // { label: "Calendar", href: "/calendar" },
  // { label: "Stats", href: "/stats" },
  // { label: "Account", href: "/account" },
];
