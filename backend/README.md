# Python backend MVP обучающей платформы

Параллельная версия backend на Python 3.12, FastAPI, SQLAlchemy 2.x, Alembic и PostgreSQL. Java backend остается в папке `backend`, эта версия живет отдельно в `backend-py`.

## Установка

```bash
cd backend-py
python -m venv .venv
pip install -r requirements.txt
```

## Конфигурация

Создайте `.env` рядом с `.env.example` или задайте переменные окружения:

```env
DATABASE_URL=postgresql+psycopg://lesson:lesson@localhost:5432/lesson
JWT_SECRET=change-this-secret-key
JWT_EXPIRATION_MINUTES=1440
CONTENT_DIR=../content
APP_PORT=8080
ADMIN_LOGIN=admin
ADMIN_PASSWORD=secret
```

`CONTENT_DIR=../content` подходит для запуска из папки `backend-py`.

## Миграции Alembic

```bash
alembic upgrade head
```

Стартовая миграция создает UUID-схему PostgreSQL и добавляет администратора.

## Запуск локально

```bash
uvicorn app.main:app --reload
```

При старте приложение проверяет `content/lessons` и `content/tests`, валидирует манифест из трех уроков и импортирует курс в базу. Отдельная команда импорта не нужна: перезапуск backend повторно синхронизирует уроки и тесты с файлами.

## API-документация

- Swagger UI: http://localhost:8080/docs
- ReDoc: http://localhost:8080/redoc
- OpenAPI JSON: http://localhost:8080/openapi.json

## Демо-логин

- login: `admin`
- password: `secret`
- role: `ADMIN`
