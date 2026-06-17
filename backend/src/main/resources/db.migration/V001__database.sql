--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql
-- Форматированный SQL для Liquibase. Технические директивы `liquibase formatted sql` и `changeset` менять нельзя.

--changeset istok:001-create-users logicalFilePath:db/changelog/db.changelog-master.sql
--validCheckSum: 9:87101dbcace72e1dc92df4b1e39290b1
-- Создание таблицы пользователей.
CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    login         VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    status        VARCHAR(30)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

--changeset istok:002-create-users-login-index logicalFilePath:db/changelog/db.changelog-master.sql
--validCheckSum: 9:7fea4942510933577e87b6a75a965da2
-- Создание уникального индекса на логин пользователя.
CREATE UNIQUE INDEX ux_users_login ON users (login);

--changeset istok:003-create-admin logicalFilePath:db/changelog/db.changelog-master.sql
--validCheckSum: 9:3d81239d62cc46ffc5724332a11fcc11
-- Добавление администратора по умолчанию.
INSERT INTO users (name, login, password_hash, role, status, created_at, updated_at)
VALUES ('Администратор',
        'admin',
        '$2a$10$kFPyjFBAOs9HlmskbGrz6uknEnfnXa8quq4WPzHn/lP.qi7t3OAtq',
        'ADMIN',
        'ACTIVE',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

--changeset istok:004-create-course-content-progress logicalFilePath:db/changelog/db.changelog-master.sql
--validCheckSum: 9:6ecf0f433f4501a051ee379d018be593
-- Создание таблиц курсов, уроков, тестов и прогресса пользователей.
CREATE TABLE courses
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT         NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);

CREATE TABLE lessons
(
    id               BIGSERIAL PRIMARY KEY,
    course_id        BIGINT       NOT NULL REFERENCES courses (id),
    position         INTEGER      NOT NULL,
    title            VARCHAR(255) NOT NULL,
    markdown_content TEXT         NOT NULL,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    CONSTRAINT uq_lessons_course_position UNIQUE (course_id, position)
);

CREATE TABLE test_questions
(
    id         BIGSERIAL PRIMARY KEY,
    lesson_id  BIGINT    NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    position   INTEGER   NOT NULL,
    text       TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE test_answers
(
    id          BIGSERIAL PRIMARY KEY,
    question_id BIGINT    NOT NULL REFERENCES test_questions (id) ON DELETE CASCADE,
    position    INTEGER   NOT NULL,
    text        TEXT      NOT NULL,
    correct     BOOLEAN   NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL
);

CREATE TABLE lesson_pass_rules
(
    id           BIGSERIAL PRIMARY KEY,
    lesson_id    BIGINT    NOT NULL UNIQUE REFERENCES lessons (id) ON DELETE CASCADE,
    pass_percent INTEGER   NOT NULL,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP NOT NULL
);

CREATE TABLE lesson_progress
(
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    lesson_id          BIGINT    NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    best_score_percent INTEGER   NOT NULL,
    passed             BOOLEAN   NOT NULL,
    completed_at       TIMESTAMP NULL,
    created_at         TIMESTAMP NOT NULL,
    updated_at         TIMESTAMP NOT NULL,
    CONSTRAINT uq_lesson_progress_user_lesson UNIQUE (user_id, lesson_id)
);

--changeset istok:005-convert-bigint-ids-to-uuid logicalFilePath:db/changelog/db.changelog-master.sql
-- Перевод всех первичных и внешних ключей с BIGINT на UUID с сохранением уже загруженных данных.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE users
    ADD COLUMN id_uuid UUID;
UPDATE users
SET id_uuid = gen_random_uuid();
ALTER TABLE users
    ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE users
    ALTER COLUMN id_uuid SET DEFAULT gen_random_uuid();

ALTER TABLE courses
    ADD COLUMN id_uuid UUID;
UPDATE courses
SET id_uuid = gen_random_uuid();
ALTER TABLE courses
    ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE courses
    ALTER COLUMN id_uuid SET DEFAULT gen_random_uuid();

ALTER TABLE lessons
    ADD COLUMN id_uuid UUID;
ALTER TABLE lessons
    ADD COLUMN course_id_uuid UUID;
UPDATE lessons
SET id_uuid = gen_random_uuid();
UPDATE lessons AS lesson
SET course_id_uuid = course.id_uuid
FROM courses AS course
WHERE lesson.course_id = course.id;
ALTER TABLE lessons
    ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE lessons
    ALTER COLUMN id_uuid SET DEFAULT gen_random_uuid();
ALTER TABLE lessons
    ALTER COLUMN course_id_uuid SET NOT NULL;

ALTER TABLE test_questions
    ADD COLUMN id_uuid UUID;
ALTER TABLE test_questions
    ADD COLUMN lesson_id_uuid UUID;
UPDATE test_questions
SET id_uuid = gen_random_uuid();
UPDATE test_questions AS question
SET lesson_id_uuid = lesson.id_uuid
FROM lessons AS lesson
WHERE question.lesson_id = lesson.id;
ALTER TABLE test_questions
    ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE test_questions
    ALTER COLUMN id_uuid SET DEFAULT gen_random_uuid();
ALTER TABLE test_questions
    ALTER COLUMN lesson_id_uuid SET NOT NULL;

ALTER TABLE test_answers
    ADD COLUMN id_uuid UUID;
ALTER TABLE test_answers
    ADD COLUMN question_id_uuid UUID;
UPDATE test_answers
SET id_uuid = gen_random_uuid();
UPDATE test_answers AS answer
SET question_id_uuid = question.id_uuid
FROM test_questions AS question
WHERE answer.question_id = question.id;
ALTER TABLE test_answers
    ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE test_answers
    ALTER COLUMN id_uuid SET DEFAULT gen_random_uuid();
ALTER TABLE test_answers
    ALTER COLUMN question_id_uuid SET NOT NULL;

ALTER TABLE lesson_pass_rules
    ADD COLUMN id_uuid UUID;
ALTER TABLE lesson_pass_rules
    ADD COLUMN lesson_id_uuid UUID;
UPDATE lesson_pass_rules
SET id_uuid = gen_random_uuid();
UPDATE lesson_pass_rules AS rule
SET lesson_id_uuid = lesson.id_uuid
FROM lessons AS lesson
WHERE rule.lesson_id = lesson.id;
ALTER TABLE lesson_pass_rules
    ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE lesson_pass_rules
    ALTER COLUMN id_uuid SET DEFAULT gen_random_uuid();
ALTER TABLE lesson_pass_rules
    ALTER COLUMN lesson_id_uuid SET NOT NULL;

ALTER TABLE lesson_progress
    ADD COLUMN id_uuid UUID;
ALTER TABLE lesson_progress
    ADD COLUMN user_id_uuid UUID;
ALTER TABLE lesson_progress
    ADD COLUMN lesson_id_uuid UUID;
UPDATE lesson_progress
SET id_uuid = gen_random_uuid();
UPDATE lesson_progress AS progress
SET user_id_uuid = "user".id_uuid
FROM users AS "user"
WHERE progress.user_id = "user".id;
UPDATE lesson_progress AS progress
SET lesson_id_uuid = lesson.id_uuid
FROM lessons AS lesson
WHERE progress.lesson_id = lesson.id;
ALTER TABLE lesson_progress
    ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE lesson_progress
    ALTER COLUMN id_uuid SET DEFAULT gen_random_uuid();
ALTER TABLE lesson_progress
    ALTER COLUMN user_id_uuid SET NOT NULL;
ALTER TABLE lesson_progress
    ALTER COLUMN lesson_id_uuid SET NOT NULL;

ALTER TABLE users
    DROP COLUMN id CASCADE;
ALTER TABLE users
    RENAME COLUMN id_uuid TO id;
ALTER TABLE users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE courses
    DROP COLUMN id CASCADE;
ALTER TABLE courses
    RENAME COLUMN id_uuid TO id;
ALTER TABLE courses
    ADD CONSTRAINT courses_pkey PRIMARY KEY (id);

ALTER TABLE lessons
    DROP CONSTRAINT uq_lessons_course_position;
ALTER TABLE lessons
    DROP COLUMN course_id;
ALTER TABLE lessons
    DROP COLUMN id CASCADE;
ALTER TABLE lessons
    RENAME COLUMN id_uuid TO id;
ALTER TABLE lessons
    RENAME COLUMN course_id_uuid TO course_id;
ALTER TABLE lessons
    ADD CONSTRAINT lessons_pkey PRIMARY KEY (id);
ALTER TABLE lessons
    ADD CONSTRAINT fk_lessons_course
        FOREIGN KEY (course_id) REFERENCES courses (id);
ALTER TABLE lessons
    ADD CONSTRAINT uq_lessons_course_position
        UNIQUE (course_id, position);

ALTER TABLE test_questions
    DROP COLUMN lesson_id;
ALTER TABLE test_questions
    DROP COLUMN id CASCADE;
ALTER TABLE test_questions
    RENAME COLUMN id_uuid TO id;
ALTER TABLE test_questions
    RENAME COLUMN lesson_id_uuid TO lesson_id;
ALTER TABLE test_questions
    ADD CONSTRAINT test_questions_pkey PRIMARY KEY (id);
ALTER TABLE test_questions
    ADD CONSTRAINT fk_test_questions_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE CASCADE;

ALTER TABLE test_answers
    DROP COLUMN question_id;
ALTER TABLE test_answers
    DROP COLUMN id;
ALTER TABLE test_answers
    RENAME COLUMN id_uuid TO id;
ALTER TABLE test_answers
    RENAME COLUMN question_id_uuid TO question_id;
ALTER TABLE test_answers
    ADD CONSTRAINT test_answers_pkey PRIMARY KEY (id);
ALTER TABLE test_answers
    ADD CONSTRAINT fk_test_answers_question
        FOREIGN KEY (question_id) REFERENCES test_questions (id) ON DELETE CASCADE;

ALTER TABLE lesson_pass_rules
    DROP COLUMN lesson_id;
ALTER TABLE lesson_pass_rules
    DROP COLUMN id;
ALTER TABLE lesson_pass_rules
    RENAME COLUMN id_uuid TO id;
ALTER TABLE lesson_pass_rules
    RENAME COLUMN lesson_id_uuid TO lesson_id;
ALTER TABLE lesson_pass_rules
    ADD CONSTRAINT lesson_pass_rules_pkey PRIMARY KEY (id);
ALTER TABLE lesson_pass_rules
    ADD CONSTRAINT fk_lesson_pass_rules_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE CASCADE;
ALTER TABLE lesson_pass_rules
    ADD CONSTRAINT uq_lesson_pass_rules_lesson
        UNIQUE (lesson_id);

ALTER TABLE lesson_progress
    DROP CONSTRAINT uq_lesson_progress_user_lesson;
ALTER TABLE lesson_progress
    DROP COLUMN user_id;
ALTER TABLE lesson_progress
    DROP COLUMN lesson_id;
ALTER TABLE lesson_progress
    DROP COLUMN id;
ALTER TABLE lesson_progress
    RENAME COLUMN id_uuid TO id;
ALTER TABLE lesson_progress
    RENAME COLUMN user_id_uuid TO user_id;
ALTER TABLE lesson_progress
    RENAME COLUMN lesson_id_uuid TO lesson_id;
ALTER TABLE lesson_progress
    ADD CONSTRAINT lesson_progress_pkey PRIMARY KEY (id);
ALTER TABLE lesson_progress
    ADD CONSTRAINT fk_lesson_progress_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE lesson_progress
    ADD CONSTRAINT fk_lesson_progress_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE CASCADE;
ALTER TABLE lesson_progress
    ADD CONSTRAINT uq_lesson_progress_user_lesson
        UNIQUE (user_id, lesson_id);
