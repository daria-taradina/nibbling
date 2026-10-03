// Points at the Spring Boot backend. Set NEXT_PUBLIC_API_URL in .env.local
// (e.g. http://localhost:8080 in dev, your Render URL in prod).
const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

// Shared helper: POSTs JSON, throws ApiError with the backend's message on failure,
// and tolerates empty success bodies (e.g. 204 No Content).
async function postJson<T>(path: string, body: unknown, fallbackError: string): Promise<T> {
  const res = await fetch(`${API_URL}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    // 429 = rate limited; the backend message says how long to wait.
    throw new ApiError(data?.message ?? fallbackError, res.status);
  }
  return data as T;
}

// The backend's LoginRequest expects "email" (not username).
export function login(email: string, password: string) {
  return postJson<{ token: string }>(
    "/api/auth/login",
    { email, password },
    "Invalid email or password.",
  );
}

export function register(username: string, email: string, password: string) {
  return postJson<{ token: string }>(
    "/api/auth/register",
    { username, email, password },
    "Could not create your account.",
  );
}

// ---- Email verification ----

export function verifyEmail(token: string) {
  return postJson<{ message?: string } | null>(
    "/api/auth/verify-email",
    { token },
    "This verification link is invalid or has expired.",
  );
}

export function resendVerification(email: string) {
  return postJson<{ message?: string } | null>(
    "/api/auth/resend-verification",
    { email },
    "Could not send a new verification email.",
  );
}

// ---- Password reset ----

// The backend always answers with the same success message, whether or not
// the email has an account, so this can't be used to check who's signed up.
export function forgotPassword(email: string) {
  return postJson<{ message?: string } | null>(
    "/api/auth/forgot-password",
    { email },
    "Could not send the reset email. Try again.",
  );
}

export function resetPassword(token: string, newPassword: string) {
  return postJson<{ message?: string } | null>(
    "/api/auth/reset-password",
    { token, newPassword },
    "This reset link is invalid or has expired.",
  );
}

// ---- GitHub / Discord sign-in ----
// These are full-page links, not fetch calls. The backend sends the user to
// the provider, then back to /oauth/callback with a token.

export function githubLoginUrl() {
  return `${API_URL}/oauth2/authorization/github`;
}

export function discordLoginUrl() {
  return `${API_URL}/oauth2/authorization/discord`;
}

// Messages for /login?error=... after a failed GitHub/Discord sign-in.
export const OAUTH_ERROR_MESSAGES: Record<string, string> = {
  oauth: "Sign-in didn't finish. Try again.",
  oauth_email:
    "We couldn't get a verified email from that account. Verify your email with GitHub or Discord, then try again.",
};
