# <img width="53" alt="logo1" src="https://github.com/user-attachments/assets/133bcb1e-b467-4e1f-9e08-e039431ae077" /> Nibbling

*(working title)* — a daily, bite-sized coding puzzle app. Solve today's problem, get a Wordle-style shareable result, and grow a companion creature based on how well you did. COMP 490/L Senior Design Project.

## 💻 Tech Stack

| Layer | Choice |
|---|---|
| Frontend | Next.js (App Router), TypeScript, Tailwind CSS |
| Backend | Spring Boot (Java), Maven |
| Database | PostgreSQL (Neon / Supabase, free tier) |
| Code execution | Judge0 |
| Frontend hosting | Vercel |
| Backend hosting | Render |

## 🗂 Repo Structure

```
nibbling/
  client/   # Next.js frontend
  server/   # Spring Boot API
```

## :rocket: Getting Started

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

# Nibbling backend — registration + JWT login

Setup guide for teammates pulling this branch for the first time.

## 1. Install PostgreSQL

Download from [postgresql.org/download](https://www.postgresql.org/download/) for your OS.
During setup you'll set a password for the `postgres` superuser — remember it,
you need it once for step 2. Leave the port at the default `5432`.

## 2. Create the app's database + user

Open **SQL Shell (psql)** (installed alongside Postgres) or **pgAdmin**'s
Query Tool, and run:

```sql
CREATE DATABASE nibbling;
CREATE USER nibbling WITH PASSWORD 'devpassword';
GRANT ALL PRIVILEGES ON DATABASE nibbling TO nibbling;
```

Then connect to the new `nibbling` database specifically (`\c nibbling` in
psql, or open a new Query Tool against it in pgAdmin) and run:

```sql
GRANT ALL ON SCHEMA public TO nibbling;
```

(That last grant is needed on Postgres 15+, which locks down the `public`
schema by default.)

Use these exact values (`nibbling` / `nibbling` / `devpassword`) for local
dev — it's not shared data, everyone has their own local Postgres, but using
identical values means everyone's `launch.json` looks the same.

## 3. pom.xml — dependencies to add/verify

Already need for basic scaffold: `spring-boot-starter-web` (or
`spring-boot-starter-webmvc` on Spring Boot 4), `spring-boot-starter-data-jpa`,
`spring-boot-starter-security`, `spring-boot-starter-validation`,
`postgresql` (runtime).

Add for JWT:

```xml
<properties>
    <jjwt.version>0.12.6</jjwt.version>
</properties>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>${jjwt.version}</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>${jjwt.version}</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>${jjwt.version}</version>
    <scope>runtime</scope>
</dependency>
```

## 4. application.properties

```properties
spring.application.name=server

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Hibernate creates/updates tables from the entity classes automatically.
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

jwt.secret=${JWT_SECRET}
jwt.expiration-ms=${JWT_EXPIRATION_MS:3600000}

# Don't leak stack traces or internal exception messages into API responses.
spring.web.error.include-message=never
spring.web.error.include-stacktrace=never
spring.web.error.include-binding-errors=never
```

## 5. Environment variables (`.vscode/launch.json`)

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
        "JWT_SECRET": "<generate your own, see below>",
        "JWT_EXPIRATION_MS": "3600000"
      }
    }
  ]
}
```

Generate a `JWT_SECRET` (at least 32 random bytes, base64-encoded) — in
PowerShell:

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

The app refuses to start if the secret is shorter than 256 bits — that's
intentional. `.vscode/launch.json` has real secrets in it, so don't commit it
with your actual value; either gitignore it or keep placeholder values in the
committed version.

## 6. Run it

From the Run and Debug panel, pick the `ServerApplication` config and hit ▶.
On first run, Hibernate will create the `users` table automatically — you
should NOT see any errors about a missing table.

## 7. Why some things are the way they are

- **UUID primary key** on `users`, generated app-side
  (`GenerationType.UUID`) — a normal Hibernate 6+ feature, no Postgres
  extension needed.
- **`password` is nullable.** GitHub OAuth accounts won't have one.
- **Login by email, not username** — `CustomUserDetailsService` treats
  email as Spring Security's "username" concept.
- **JWT subject is the user's UUID, not the username** — stays valid even
  if a username changes, and the auth filter re-fetches the user from the
  DB by that id on every request, so a banned/deleted account is locked out
  immediately instead of only after the token expires.
- **Login always returns the same "Invalid email or password"** whether the
  email doesn't exist or the password is wrong — standard practice against
  user-enumeration.
- **Register returns 201**, not 200 — it created a resource.
- **`UserSummary`** in responses only has `id`, `username`, `emailVerified`
  — no email, no password hash, nothing that could leak by accident.

## 8. Endpoints

```
POST /api/auth/register  { username, email, password }  -> 201 AuthResponse
POST /api/auth/login     { email, password }             -> 200 AuthResponse
```

`AuthResponse`: `{ accessToken, tokenType, expiresInMs, user: { id, username, emailVerified } }`

Authenticated requests: `Authorization: Bearer <accessToken>`.

## 9. Not included yet — flagging, not solving

- **Refresh tokens** — a token just expires and the user re-logs in. Fine
  for now.
- **Rate limiting on `/login`** — nothing stops brute-forcing a password
  yet. Add before this is public-facing.
- **CORS origin** is hardcoded to `http://localhost:3000` in
  `SecurityConfig` — update once there's a deployed frontend URL.
- **Schema migrations** — currently Hibernate auto-manages the schema
  (`ddl-auto=update`) instead of Flyway/versioned SQL migrations. Fine
  while the team is small and moving fast; worth revisiting if
  auto-updates start causing confusing schema drift once more people are
  adding columns independently.

## Team

- Daria — [role/focus TBD]
- [Teammate 2]
- [Teammate 3]
