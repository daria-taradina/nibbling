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

// NOTE: paths/payload below are a guess at your controller shape —
// swap these to match whatever AuthController actually exposes.
export async function login(usernameOrEmail: string, password: string) {
  const res = await fetch(`${API_URL}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ usernameOrEmail, password }),
  });
  if (!res.ok) {
    const body = await res.json().catch(() => null);
    throw new ApiError(body?.message ?? "Invalid username/email or password.", res.status);
  }
  return res.json() as Promise<{ token: string }>;
}

export async function register(username: string, email: string, password: string) {
  const res = await fetch(`${API_URL}/api/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, email, password }),
  });
  if (!res.ok) {
    const body = await res.json().catch(() => null);
    throw new ApiError(body?.message ?? "Could not create your account.", res.status);
  }
  return res.json() as Promise<{ token: string }>;
}

// Spring Security's default OAuth2 client convention — this path should
// work as soon as the GitHub provider is registered on the backend,
// no frontend change needed.
export function githubLoginUrl() {
  return `${API_URL}/oauth2/authorization/github`;
}