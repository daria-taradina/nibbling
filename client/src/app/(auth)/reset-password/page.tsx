"use client";

import { FormEvent, Suspense, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { AuthCard } from "@/components/auth/AuthCard";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { resetPassword, ApiError } from "@/lib/api";

const MIN_PASSWORD_LENGTH = 8;

function ResetPasswordForm() {
  const router = useRouter();
  // The emailed link looks like /reset-password?token=abc123
  const token = useSearchParams().get("token");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  if (!token) {
    return (
      <p className="text-sm text-red-600 dark:text-red-400">
        This reset link is missing its token. Request a new link from the login page.
      </p>
    );
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (password.length < MIN_PASSWORD_LENGTH) {
      setError(`Password must be at least ${MIN_PASSWORD_LENGTH} characters.`);
      return;
    }
    if (password !== confirmPassword) {
      setError("Passwords don't match.");
      return;
    }

    setLoading(true);
    try {
      await resetPassword(token!, password);
      setDone(true);
      setTimeout(() => router.push("/login"), 2000);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Something went wrong. Try again.");
    } finally {
      setLoading(false);
    }
  }

  if (done) {
    return <p className="text-sm">Password updated. Taking you to log in…</p>;
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <Input
        label="New password"
        name="password"
        type="password"
        autoComplete="new-password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        required
      />
      <Input
        label="Confirm new password"
        name="confirmPassword"
        type="password"
        autoComplete="new-password"
        value={confirmPassword}
        onChange={(e) => setConfirmPassword(e.target.value)}
        required
      />

      {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}

      <Button type="submit" loading={loading}>
        Save new password
      </Button>
    </form>
  );
}

export default function ResetPasswordPage() {
  return (
    <AuthCard
      title="Choose a new password"
      subtitle="Pick something you haven't used here before."
      footerText="Changed your mind?"
      footerLinkText="Log in"
      footerLinkHref="/login"
    >
      {/* useSearchParams needs a Suspense boundary in the App Router */}
      <Suspense fallback={null}>
        <ResetPasswordForm />
      </Suspense>
    </AuthCard>
  );
}
