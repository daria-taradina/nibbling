// Points at the Spring Boot backend. Set NEXT_PUBLIC_API_URL in .env.local
// (e.g. http://localhost:8080 in dev, your Render URL in prod).
import { getToken } from "@/lib/auth";

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

export interface UserSummary {
  id: string;
  username: string;
  emailVerified: boolean;
}

// What /login and /register return. NOTE the field is "accessToken",
// not "token" (only the GitHub/Discord redirect uses #token=...).
export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresInMs: number;
  user: UserSummary;
}

// The backend's LoginRequest expects "email" (not username).
export function login(email: string, password: string) {
  return postJson<AuthResponse>(
    "/api/auth/login",
    { email, password },
    "Invalid email or password.",
  );
}

export function register(username: string, email: string, password: string) {
  return postJson<AuthResponse>(
    "/api/auth/register",
    { username, email, password },
    "Could not create your account.",
  );
}

// ---- Current user ----

// Needs the JWT; a 401 means it's missing, expired or the account is gone.
export async function getMe(token: string): Promise<UserSummary> {
  const res = await fetch(`${API_URL}/api/auth/me`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    throw new ApiError(data?.message ?? "Could not load your account.", res.status);
  }
  return data as UserSummary;
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

// ---- Daily quiz ----

export type ChallengeState = "NOT_STARTED" | "IN_PROGRESS" | "SUBMITTED";
export type Tier = "FEAST" | "MEAL" | "SNACK";

export interface QuizOption {
  key: string;
  label: string;
}

export interface QuizQuestion {
  id: number;
  position: number;
  topic: string;
  difficulty: number; // 1 easy, 2 medium, 3 hard
  kind: string;
  promptMd: string;
  codeSnippet: string | null;
  options: QuizOption[];
}

export interface QuizReview {
  questionId: number;
  selectedKeys: string[];
  correctKeys: string[];
  correct: boolean;
  explanationMd: string;
}

export interface QuizResult {
  correctCount: number;
  total: number;
  score: number;
  tier: Tier | null; // null = auto-submitted with no answers (a Miss)
  xp: number;
  submitReason: "USER" | "DEADLINE";
  review: QuizReview[];
}

// questionId (as a string, JSON keys always are) -> selected option keys
export type AnswersMap = Record<string, string[]>;

export interface TodayChallenge {
  puzzleDate: string; // "2026-10-19"
  type: string;
  state: ChallengeState;
  deadlineAt: string; // ISO instant, end of the puzzle day in Los Angeles
  questionCount: number;
  questions: QuizQuestion[]; // empty until the quiz is started
  answers: AnswersMap;
  result: QuizResult | null; // only once submitted
}

// Authenticated JSON request. A 401 throws ApiError(401): callers sign the user out.
async function authedJson<T>(
  path: string,
  method: "GET" | "POST" | "PUT",
  body?: unknown,
  keepalive = false,
): Promise<T> {
  const token = getToken();
  if (!token) throw new ApiError("Not signed in.", 401);
  const res = await fetch(`${API_URL}${path}`, {
    method,
    headers: {
      Authorization: `Bearer ${token}`,
      ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
    keepalive, // lets a last save finish while the page is closing
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    throw new ApiError(data?.message ?? "Something went wrong.", res.status);
  }
  return data as T;
}

export function getToday() {
  return authedJson<TodayChallenge>("/api/challenges/today", "GET");
}

// Idempotent: calling it twice still gives one attempt.
export function startToday() {
  return authedJson<TodayChallenge>("/api/challenges/today/start", "POST");
}

// Sends the whole draft every time; the last write wins. If the day ended while
// answering, state comes back "SUBMITTED" and the caller should reload.
export function saveDraft(answers: AnswersMap, keepalive = false) {
  return authedJson<{ state: ChallengeState; deadlineAt: string }>(
    "/api/challenges/today/draft",
    "PUT",
    { answers },
    keepalive,
  );
}

// Passing the answers saves them first, so a not-yet-sent draft can't be lost.
export function submitToday(answers?: AnswersMap) {
  return authedJson<TodayChallenge>("/api/challenges/today/submit", "POST", answers ? { answers } : undefined);
}

// ---- Progress: XP, streak, calendar ----

// A day the player has an attempt for. Days with no attempt aren't listed.
export interface DayResult {
  date: string; // "2026-10-19"
  status: "SUBMITTED" | "IN_PROGRESS";
  tier: Tier | null; // null on SUBMITTED = a Miss
}

export interface StatsResponse {
  today: string; // puzzle date in Los Angeles, the source of truth for "today"
  totalXp: number;
  level: number;
  xpIntoLevel: number;
  xpPerLevel: number;
  currentStreak: number;
  longestStreak: number;
  firstPlayedOn: string | null;
  week: DayResult[]; // Monday..Sunday of the current week
}

export interface CalendarResponse {
  month: string; // "2026-10"
  today: string;
  firstPlayedOn: string | null;
  days: DayResult[];
}

export function getStats() {
  return authedJson<StatsResponse>("/api/challenges/stats", "GET");
}

// No month = the current month.
export function getCalendar(month?: string) {
  return authedJson<CalendarResponse>(`/api/challenges/calendar${month ? `?month=${month}` : ""}`, "GET");
}
