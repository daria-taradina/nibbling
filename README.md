# <img width="53" alt="logo1" src="https://github.com/user-attachments/assets/133bcb1e-b467-4e1f-9e08-e039431ae077" /> Nibbling

*(working title)* — a daily, bite-sized coding puzzle app. Solve today's problem, get a Wordle-style shareable result, and grow a companion creature based on how well you did. COMP 490/L Senior Design Project.

## Tech Stack

| Layer | Choice |
|---|---|
| Frontend | Next.js (App Router), TypeScript, Tailwind CSS |
| Backend | Spring Boot (Java), Maven |
| Database | PostgreSQL (Neon / Supabase, free tier) |
| Code execution | Judge0 |
| Frontend hosting | Vercel |
| Backend hosting | Render |

## Repo Structure

```
nibbling/
  client/   # Next.js frontend
  server/   # Spring Boot API
```

## Getting Started

### Prerequisites

- Node.js (LTS)
- Java 25 (or matching JDK installed locally)
- Maven (or use the included `./mvnw` wrapper — no separate install needed)
- A PostgreSQL instance (Neon or Supabase free tier)

### Frontend (`client`)

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
