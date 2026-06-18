# MVP обучающей платформы

Веб-платформа для прохождения одного обучающего курса с уроками, тестами, прогрессом, ролями пользователей и
сертификатом после завершения курса.

Полные документы:

- [Техническое задание MVP](./Техническое%20задание%20MVP.md)
- [Руководство локального запуска](./Руководство%20локального%20запуска.md)
- [Предложения по развитию](./Предложения%20по%20развитию.md)

## Стек

- Backend: Python, FastAPI, SQLAlchemy, Alembic, JWT.
- Frontend: React, TypeScript, Vite.
- Database: PostgreSQL.
- Infrastructure: Docker, Docker Compose, nginx для Docker-сборки frontend.

## Быстрый запуск для демонстрации

Вариант поднимает весь проект в Docker: PostgreSQL, backend и frontend.

```powershell
cd infra
docker compose up --build
```

После старта:

- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- Swagger: http://localhost:8080/docs
- ReDoc: http://localhost:8080/redoc

Демо-пользователь:

```text
login: admin
password: secret
```

## Разработка

Для разработки обычно запускаются:

- PostgreSQL в Docker через `infra/docker-compose.yml`.
- Backend локально из `backend`.
- Frontend локально из `frontend`.

Подробные команды для Windows и терминала PyCharm описаны
в [руководстве локального запуска](./Руководство%20локального%20запуска.md).

## Структура проекта

- `backend/` - FastAPI-приложение, SQLAlchemy-модели, Alembic-миграции, бизнес-логика.
- `frontend/` - React/Vite-приложение.
- `content/` - markdown-уроки и JSON-тесты, которые импортируются backend при запуске.
- `infra/` - Docker Compose и переменные окружения для Docker-сценария.
- `Руководство локального запуска.md` - пошаговый запуск демо и dev-режима.
- `Техническое задание MVP.md` - описание требований MVP.

## Как ориентироваться в коде

Backend разложен по функциональным зонам:

- `app/auth` - авторизация и JWT.
- `app/users` - пользователи, роли, администрирование.
- `app/course` - курс, уроки, тесты, импорт контента.
- `app/progress` - прогресс прохождения.
- `app/certificate` - генерация сертификата.
- `app/core`, `app/common`, `app/db` - конфигурация, ошибки, безопасность и доступ к базе.

Frontend разложен по слоям:

- `src/app` - корневое приложение и маршрутизация.
- `src/pages` - страницы.
- `src/features` - API, модели и UI по доменным фичам.
- `src/shared` - общий API-клиент, UI-компоненты, стили и конфигурация.

Если нужно найти обработчик API, начинайте со Swagger: http://localhost:8080/docs. Затем ищите соответствующий router в
`backend/app/*/router.py`, после него service и repository.

