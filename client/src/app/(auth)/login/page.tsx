"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { AuthCard } from "@/components/auth/AuthCard";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { GithubIcon } from "@/components/icons/GithubIcon";
import { login, githubLoginUrl, ApiError } from "@/lib/api";

export default function LoginPage() {
  const router = useRouter();
  const [usernameOrEmail, setUsernameOrEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const { token } = await login(usernameOrEmail, password);
      // MVP: localStorage is simplest to wire today. Once the backend can
      // set an httpOnly cookie on /api/auth/login, switch to that instead —
      // it's not readable by JS, so it holds up better against XSS.
      localStorage.setItem("nibbling_token", token);
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
        <Input
          label="Username or email"
          name="usernameOrEmail"
          autoComplete="username"
          value={usernameOrEmail}
          onChange={(e) => setUsernameOrEmail(e.target.value)}
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

        {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}

        <Button type="submit" loading={loading}>
          Log in
        </Button>
      </form>

      <div className="my-5 flex items-center gap-3">
        <div className="h-px flex-1 bg-cream-border dark:bg-ink-border" />
        <span className="text-xs text-ink-bg/40 dark:text-cream-bg/40">or</span>
        <div className="h-px flex-1 bg-cream-border dark:bg-ink-border" />
      </div>

      <a href={githubLoginUrl()}>
        <Button type="button" variant="oauth">
          <GithubIcon />
          Continue with GitHub
        </Button>
      </a>
    </AuthCard>
  );
}