# <img width="44" alt="Nibbling logo" src="https://github.com/user-attachments/assets/133bcb1e-b467-4e1f-9e08-e039431ae077" /> Nibbling

A daily bite-sized coding quiz app. Answer 5 questions, earn XP, keep a streak, share your result. COMP 490/L Senior Design Project.

<img width="3290" height="1696" alt="image" src="https://github.com/user-attachments/assets/35baab95-9336-4b69-8236-d6387e703599" />

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

### Backend (`server`)

The backend reads its database credentials from environment variables — nothing is hardcoded in `application.properties`. Set the following before running:

```
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USERNAME=<your-db-username>
DB_PASSWORD=<your-db-password>
```

**In VS Code:** set these in `server/.vscode/launch.json` under the `"env"` key (this file is gitignored, so your real values never get committed — see `server/.vscode/launch.json` for the template shape).

**In IntelliJ:** Run → Edit Configurations → Environment variables field.

Then run:

```bash
cd server
./mvnw spring-boot:run
```

Runs at `http://localhost:8080`.

## Environment Variables

No `.env` files are committed anywhere in this repo. Each person sets their own local values through their IDE's run configuration (see above). Production values (Render, Vercel) are set directly in each platform's dashboard.

## Status

🚧 Early scaffold — client and server run independently, not yet wired together. No database connected yet.

## Team

- Daria — [role/focus TBD]
- [Teammate 2]
- [Teammate 3]
