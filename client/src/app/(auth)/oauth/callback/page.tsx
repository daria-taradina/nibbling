"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { AuthCard } from "@/components/auth/AuthCard";
import { setToken } from "@/lib/auth";

// The backend sends people here after GitHub/Discord sign-in:
//   /oauth/callback#token=...
// The token is in the "#" part, which browsers never send to any server.
export default function OAuthCallbackPage() {
  const router = useRouter();

  useEffect(() => {
    const token = new URLSearchParams(window.location.hash.slice(1)).get("token");

    // Wipe the token from the address bar and browser history right away.
    window.history.replaceState(null, "", window.location.pathname);

    if (!token) {
      // Login already knows how to show this error.
      router.replace("/login?error=oauth");
      return;
    }
    setToken(token);
    router.replace("/");
  }, [router]);

  return (
    <AuthCard
      title="Signing you in"
      subtitle="Your companion is waking up."
      footerText="Having trouble?"
      footerLinkText="Back to log in"
      footerLinkHref="/login"
    >
      <p className="text-sm">Just a moment…</p>
    </AuthCard>
  );
}
