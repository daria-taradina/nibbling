"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { AuthCard } from "@/components/auth/AuthCard";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { GithubIcon } from "@/components/icons/GithubIcon";
import { DiscordIcon } from "@/components/icons/DiscordIcon";
import { register, githubLoginUrl, discordLoginUrl, ApiError } from "@/lib/api";
import { setToken } from "@/lib/auth";

export default function RegisterPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (password !== confirmPassword) {
      setError("Passwords don't match.");
      return;
    }

    setLoading(true);
    try {
      const { accessToken } = await register(username, email, password);
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
      title="Start your streak"
      subtitle="One problem a day. Watch your companion grow."
      footerText="Already have an account?"
      footerLinkText="Log in"
      footerLinkHref="/login"
    >
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <Input
          label="Username"
          name="username"
          autoComplete="username"
          maxLength={30}
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
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
          autoComplete="new-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <Input
          label="Confirm password"
          name="confirmPassword"
          type="password"
          autoComplete="new-password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          required
        />

        {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}

        <Button type="submit" loading={loading}>
          Create account
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
