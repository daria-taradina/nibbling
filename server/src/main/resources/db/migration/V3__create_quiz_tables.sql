-- Daily quiz: question bank, the 5 questions chosen for each day, and one
-- attempt per user per day. Questions are loaded by R__seed_questions.sql.

CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(100) NOT NULL,
    topic VARCHAR(50) NOT NULL,
    difficulty INTEGER NOT NULL,
    kind VARCHAR(20) NOT NULL,          -- MCQ | CODE_OUTPUT | FIND_BUG
    prompt_md TEXT NOT NULL,
    code_snippet TEXT,
    explanation_md TEXT NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'DRAFT',   -- DRAFT | APPROVED
    CONSTRAINT uq_questions_slug UNIQUE (slug),
    CONSTRAINT ck_questions_difficulty CHECK (difficulty BETWEEN 1 AND 3)
);

CREATE TABLE question_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    option_key VARCHAR(10) NOT NULL,
    position INTEGER NOT NULL,
    label TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    CONSTRAINT uq_question_options_key UNIQUE (question_id, option_key)
);

CREATE TABLE daily_challenges (
    puzzle_date DATE PRIMARY KEY,
    type VARCHAR(10) NOT NULL DEFAULT 'QUIZ'       -- QUIZ now, CODE later
);

CREATE TABLE daily_challenge_questions (
    id BIGSERIAL PRIMARY KEY,
    puzzle_date DATE NOT NULL REFERENCES daily_challenges (puzzle_date) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    question_id BIGINT NOT NULL REFERENCES questions (id),
    CONSTRAINT uq_daily_position UNIQUE (puzzle_date, position),
    CONSTRAINT uq_daily_question UNIQUE (puzzle_date, question_id)
);

CREATE TABLE attempts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    puzzle_date DATE NOT NULL REFERENCES daily_challenges (puzzle_date),
    status VARCHAR(12) NOT NULL,                   -- IN_PROGRESS | SUBMITTED
    submit_reason VARCHAR(10),                     -- USER | DEADLINE
    started_at TIMESTAMPTZ NOT NULL,
    deadline_at TIMESTAMPTZ NOT NULL,
    submitted_at TIMESTAMPTZ,
    correct_count INTEGER,
    total INTEGER,
    score DOUBLE PRECISION,
    tier VARCHAR(10),                              -- FEAST | MEAL | SNACK; NULL = no answers = Miss
    xp INTEGER,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_attempt_user_day UNIQUE (user_id, puzzle_date)
);

CREATE INDEX idx_attempts_open ON attempts (status, deadline_at);

-- The draft: one row per selected option, replaced on every save.
CREATE TABLE attempt_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id UUID NOT NULL REFERENCES attempts (id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions (id),
    option_key VARCHAR(10) NOT NULL,
    CONSTRAINT uq_attempt_answer UNIQUE (attempt_id, question_id, option_key)
);
