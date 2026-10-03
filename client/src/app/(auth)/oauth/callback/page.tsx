"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AuthCard } from "@/components/auth/AuthCard";

// The backend sends people here after GitHub/Discord sign-in:
//   /oauth/callback#token=...
// The token is in the "#" part, which browsers never send to any server.
export default function OAuthCallbackPage() {
  const router = useRouter();
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    const token = new URLSearchParams(window.location.hash.slice(1)).get("token");

    // Wipe the token from the address bar and browser history right away.
    window.history.replaceState(null, "", window.location.pathname);

    if (!token) {
      setFailed(true);
      return;
    }
    localStorage.setItem("nibbling_token", token);
    router.replace("/");
  }, [router]);

  return (
    <AuthCard
      title={failed ? "Sign-in didn't finish" : "Signing you in"}
      subtitle={failed ? "No sign-in token came back." : "Your companion is waking up."}
      footerText="Having trouble?"
      footerLinkText="Back to log in"
      footerLinkHref="/login"
    >
      <p className="text-sm">
        {failed ? "Go back to the login page and try again." : "Just a moment…"}
      </p>
    </AuthCard>
  );
}
