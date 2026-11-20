// Single home for the JWT's localStorage key so login, OAuth callback and the
// app shell can't drift apart. (Swap to an httpOnly cookie later and this is
// the only file that needs to change.)
const TOKEN_KEY = "nibbling_token";

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}
