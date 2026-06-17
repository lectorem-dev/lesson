from functools import lru_cache
from pathlib import Path
import os

from dotenv import load_dotenv


load_dotenv()


class Settings:
    def __init__(self) -> None:
        self.database_url = os.getenv(
            "DATABASE_URL",
            "postgresql+psycopg://lesson:lesson@localhost:5432/lesson",
        )
        self.jwt_secret = os.getenv("JWT_SECRET", "change-this-secret-key")
        self.jwt_expiration_minutes = int(os.getenv("JWT_EXPIRATION_MINUTES", "1440"))
        self.content_dir = Path(os.getenv("CONTENT_DIR", "../content"))
        self.app_port = int(os.getenv("APP_PORT", "8080"))
        self.admin_login = os.getenv("ADMIN_LOGIN", "admin")
        self.admin_password = os.getenv("ADMIN_PASSWORD", "secret")
        self.cors_origins = [
            "http://localhost:3000",
            "http://localhost:5173",
            "http://127.0.0.1:5173",
        ]


@lru_cache
def get_settings() -> Settings:
    return Settings()


settings = get_settings()
