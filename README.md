# <img width="44" alt="Nibbling logo" src="https://github.com/user-attachments/assets/133bcb1e-b467-4e1f-9e08-e039431ae077" /> Nibbling

A daily bite-sized coding quiz app. Answer 5 questions, earn XP, keep a streak, share your result. COMP 490/L Senior Design Project.

## Tech stack

| Layer | Choice |
|---|---|
| Frontend | Next.js 15 (App Router), TypeScript, Tailwind CSS v4 |
| Backend | Spring Boot 4, Java 25, Maven |
| Database | PostgreSQL (Neon — free tier works fine) |
| Frontend hosting | Vercel |
| Backend hosting | Render |

## Repo structure

```
nibbling/
  client/   # Next.js frontend
  server/   # Spring Boot API
```

---

## Local setup

### Prerequisites

- Node.js LTS
- Java 25 JDK
- A PostgreSQL instance — Neon free tier or a local install both work

### 1. Database

Create a database and user. In psql or pgAdmin:

```sql
CREATE DATABASE nibbling;
CREATE USER nibbling WITH PASSWORD 'devpassword';
GRANT ALL PRIVILEGES ON DATABASE nibbling TO nibbling;
```

Then connect to the `nibbling` database and run:

```sql
GRANT ALL ON SCHEMA public TO nibbling;
```

Required on Postgres 15+ which locks the public schema by default.

The app uses `ddl-auto=update`, so Hibernate creates and updates all tables on startup — no manual SQL needed except for seed data (see below).

### 2. Backend environment variables

Nothing is hardcoded in `application.properties`. Set these before running:

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | JDBC URL | — required |
| `DB_USERNAME` | DB user | — required |
| `DB_PASSWORD` | DB password | — required |
| `JWT_SECRET` | Base64 secret, min 32 bytes | — required |
| `JWT_EXPIRATION_MS` | Token lifetime in ms | `3600000` (1 h) |
| `RESEND_API_KEY` | Resend.com API key for email | prints links to console if empty |
| `RESEND_FROM` | From address for emails | `Nibbling <onboarding@resend.dev>` |
| `FRONTEND_URL` | Next.js app URL (for email links) | `http://localhost:3000` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins | `http://localhost:3000` |
| `GITHUB_CLIENT_ID` | GitHub OAuth app client ID | `not-set` (OAuth disabled) |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth app secret | `not-set` |
| `DISCORD_CLIENT_ID` | Discord OAuth app client ID | `not-set` |
| `DISCORD_CLIENT_SECRET` | Discord OAuth app secret | `not-set` |

**VS Code** — set these in `server/.vscode/launch.json` under the `"env"` key. This file is gitignored so secrets stay local:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "ServerApplication",
      "request": "launch",
      "mainClass": "com.nibbling.server.ServerApplication",
      "env": {
        "DB_URL": "jdbc:postgresql://localhost:5432/nibbling",
        "DB_USERNAME": "nibbling",
        "DB_PASSWORD": "devpassword",
        "JWT_SECRET": "<generate — see below>",
        "JWT_EXPIRATION_MS": "3600000"
      }
    }
  ]
}
```

Generate a `JWT_SECRET` (the app refuses to start with fewer than 256 bits):

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

**IntelliJ** — Run → Edit Configurations → Environment variables.

### 3. Run the backend

```bash
cd server
./mvnw spring-boot:run    # or mvnw.cmd on Windows
```

Runs at `http://localhost:8080`. On first run Hibernate creates all tables automatically.

### 4. Seed questions

After the tables exist, run the seed file once to populate the question bank:

```bash
psql "<your-connection-string>" -f server/src/main/resources/db/migration/R__seed_questions.sql
```

Re-run it any time the file changes — every statement is an upsert, so it's safe to repeat.

If your tables are missing unique constraints (created by an older Hibernate run before the constraints were added), run these first:

```sql
ALTER TABLE questions        ADD CONSTRAINT uq_questions_slug       UNIQUE (slug);
ALTER TABLE question_options ADD CONSTRAINT uq_question_options_key UNIQUE (question_id, option_key);
ALTER TABLE attempts         ADD CONSTRAINT uq_attempt_user_day     UNIQUE (user_id, puzzle_date);
```

### 5. Frontend environment variables

Create `client/.env.local`:

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### 6. Run the frontend

```bash
cd client
npm install
npm run dev
```

Runs at `http://localhost:3000`.

---

## OAuth setup (optional for local dev)

GitHub and Discord sign-in work when both sets of credentials are set. Without them the server still starts — OAuth buttons just won't complete.

**GitHub** — create an OAuth app at github.com/settings/developers. Callback URL: `http://localhost:8080/login/oauth2/code/github`

**Discord** — create an application at discord.com/developers. Redirect URL: `http://localhost:8080/login/oauth2/code/discord`

---

## API overview

All endpoints under `/api/challenges/*` and `/api/auth/me` require a valid JWT in the `Authorization: Bearer <token>` header.

### Auth

```
POST /api/auth/register            { username, email, password }  → 201 AuthResponse
POST /api/auth/login               { email, password }            → 200 AuthResponse
POST /api/auth/forgot-password     { email }                      → 200
POST /api/auth/reset-password      { token, newPassword }         → 200
POST /api/auth/verify-email        { token }                      → 200
POST /api/auth/resend-verification { email }                      → 200
GET  /api/auth/me                                                 → 200 UserSummary
```

`AuthResponse`: `{ accessToken, tokenType, expiresInMs, user: { id, username, emailVerified } }`

### Daily quiz

```
GET  /api/challenges/today           → TodayResponse (NOT_STARTED / IN_PROGRESS / SUBMITTED)
POST /api/challenges/today/start     → TodayResponse
PUT  /api/challenges/today/draft     { answers }   → SaveDraftResponse
POST /api/challenges/today/submit    { answers? }  → TodayResponse
POST /api/challenges/today/share                   → ShareResultView
```

### Stats & calendar

```
GET  /api/challenges/stats              → StatsResponse (XP, level, streaks, this week)
GET  /api/challenges/calendar?month=    → CalendarResponse (per-day results for a month)
```

### Share (public — no auth required)

```
GET  /api/share/{token}  → ShareResultView
```

---

## Game mechanics

**Scoring** — 5 questions per day (2 easy, 2 medium, 1 hard). Each correct answer earns 5 XP, so a perfect quiz is 25 XP. Tier is determined by weighted score:

| Score | Tier |
|---|---|
| ≥ 90% | 🍱 Feast |
| ≥ 60% | 🍙 Meal |
| < 60% (but answered) | 🍡 Snack |
| No answers | Miss (0 XP) |

**Streak bonus** — +25 XP every 7-day streak milestone (7, 14, 21…).

**Levels**

| Level | Name | XP needed |
|---|---|---|
| 1 | Hatchling | 0 |
| 2 | Learner | 100 |
| 3 | Coder | 300 |
| 4 | Developer | 600 |
| 5 | Code Master | 1000 |

**Puzzle day** — resets at midnight Pacific time (America/Los_Angeles), Wordle-style. An attempt not submitted by midnight is auto-submitted with whatever answers were saved.

---

## Schema notes

The app uses `ddl-auto=update` (Hibernate manages the schema). Flyway migrations in `server/src/main/resources/db/migration/` exist as documentation and for future use but are currently disabled.

Tables created on first run: `users`, `auth_tokens`, `oauth_accounts`, `questions`, `question_options`, `daily_challenges`, `daily_challenge_questions`, `attempts`, `attempt_answers`.

---

## 💚 Contributors

<table>
  <tr>
    <td align="center" width="340">
      <a href="https://github.com/daria-taradina">
        <img src="https://github.com/daria-taradina.png?size=150" width="90" alt="Daria Taradina" />
      </a>
      <br /><br />
      <a href="https://github.com/dariatarad">
        <img src="https://img.shields.io/badge/dariataradina-e8b4d0?style=for-the-badge&logo=github&logoColor=333" />
      </a>
      <br />
      <sub></sub>
    </td>
    <td align="center" width="340">
      <a href="https://github.com/realfarshadazam-lab">
        <img src="https://github.com/realfarshadazam-lab.png?size=150" width="90" alt="Farshad Azam" />
      </a>
      <br /><br />
      <a href="https://github.com/realfarshadazam-lab">
        <img src="https://img.shields.io/badge/realfarshadazamlab-7DD17D?style=for-the-badge&logo=github&logoColor=333" />
      </a>
      <br />
      <sub></sub>
    </td>
  </tr>
</table>
