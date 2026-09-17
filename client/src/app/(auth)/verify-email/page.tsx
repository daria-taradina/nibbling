"use client";

import { FormEvent, Suspense, useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { AuthCard } from "@/components/auth/AuthCard";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { verifyEmail, resendVerification, ApiError } from "@/lib/api";

type Status = "checking" | "success" | "error";

function ResendForm() {
  const [email, setEmail] = useState("");
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await resendVerification(email);
      setSent(true);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Something went wrong. Try again.");
    } finally {
      setLoading(false);
    }
  }

  if (sent) {
    return <p className="text-sm">If that account needs verifying, a new link is on its way.</p>;
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <Input
        label="Email"
        name="email"
        type="email"
        autoComplete="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        required
      />
      {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}
      <Button type="submit" loading={loading}>
        Send a new link
      </Button>
    </form>
  );
}

function VerifyEmailStatus() {
  // The emailed link looks like /verify-email?token=abc123
  const token = useSearchParams().get("token");
  const [status, setStatus] = useState<Status>(token ? "checking" : "error");
  const [message, setMessage] = useState(
    token ? "" : "This verification link is missing its token.",
  );
  // React runs effects twice in dev mode. Tokens are single-use, so a second
  // call would fail and show an error even though verification worked.
  const alreadyCalled = useRef(false);

  useEffect(() => {
    if (!token || alreadyCalled.current) return;
    alreadyCalled.current = true;

    verifyEmail(token)
      .then(() => setStatus("success"))
      .catch((err) => {
        setStatus("error");
        setMessage(err instanceof ApiError ? err.message : "Something went wrong. Try again.");
      });
  }, [token]);

  if (status === "checking") {
    return <p className="text-sm">Checking your link…</p>;
  }

  if (status === "success") {
    return (
      <div className="flex flex-col gap-3 text-sm">
        <p>Your email is verified. You're all set.</p>
        <Link href="/" className="text-green-600 hover:underline dark:text-green-400">
          Go to today's challenge
        </Link>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-4 text-sm">
      <p className="text-red-600 dark:text-red-400">{message}</p>
      <p>Enter your email and we'll send you a fresh link.</p>
      <ResendForm />
    </div>
  );
}

export default function VerifyEmailPage() {
  return (
    <AuthCard
      title="Verify your email"
      subtitle="One quick check and your companion can start growing."
      footerText="Need to sign in?"
      footerLinkText="Log in"
      footerLinkHref="/login"
    >
      <Suspense fallback={null}>
        <VerifyEmailStatus />
      </Suspense>
    </AuthCard>
  );
}
