"use client";

import { FormEvent, Suspense, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { AuthCard } from "@/components/auth/AuthCard";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { setToken } from "@/lib/auth";
import { GithubIcon } from "@/components/icons/GithubIcon";
import { DiscordIcon } from "@/components/icons/DiscordIcon";
import {
  login,
  githubLoginUrl,
  discordLoginUrl,
  OAUTH_ERROR_MESSAGES,
  ApiError,
} from "@/lib/api";

function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // GitHub/Discord sign-in sends us back with ?error=... — derived, not stored.
  const oauthCode = searchParams.get("error");
  const oauthError = oauthCode ? (OAUTH_ERROR_MESSAGES[oauthCode] ?? OAUTH_ERROR_MESSAGES.oauth) : null;
  const shownError = error ?? oauthError;

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const { accessToken } = await login(email, password);
      // MVP: localStorage is simplest to wire today. Once the backend can
      // set an httpOnly cookie on /api/auth/login, switch to that instead —
      // it's not readable by JS, so it holds up better against XSS.
      setToken(accessToken);
      router.push("/");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Something went wrong. Try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthCard
      title="Welcome back!"
      subtitle="Solve today's challenge and keep your streak going."
      footerText="New here?"
      footerLinkText="Create an account"
      footerLinkHref="/register"
    >
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        {/* The backend logs in by email only, so this field asks for email. */}
        <Input
          label="Email"
          name="email"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <Input
          label="Password"
          name="password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <Link
          href="/forgot-password"
          className="-mt-2 self-end text-sm text-green-600 hover:underline dark:text-green-400"
        >
          Forgot password?
        </Link>

        {shownError && <p className="text-sm text-red-600 dark:text-red-400">{shownError}</p>}

        <Button type="submit" loading={loading}>
          Log in
        </Button>
      </form>

      <div className="my-5 flex items-center gap-3">
        <div className="h-px flex-1 bg-cream-border dark:bg-ink-border" />
        <span className="text-xs text-ink-bg/40 dark:text-cream-bg/40">or</span>
        <div className="h-px flex-1 bg-cream-border dark:bg-ink-border" />
      </div>

      <div className="flex flex-col gap-3">
        <a href={githubLoginUrl()}>
          <Button type="button" variant="oauth">
            <GithubIcon />
            Continue with GitHub
          </Button>
        </a>
        <a href={discordLoginUrl()}>
          <Button type="button" variant="oauth">
            <DiscordIcon />
            Continue with Discord
          </Button>
        </a>
      </div>
    </AuthCard>
  );
}

export default function LoginPage() {
  return (
    <Suspense>
      <LoginForm />
    </Suspense>
  );
}
