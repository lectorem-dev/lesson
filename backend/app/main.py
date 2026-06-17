from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.auth.router import router as auth_router
from app.certificate.router import router as certificate_router
from app.core.config import settings
from app.core.errors import register_error_handlers
from app.course.import_content import import_course_content
from app.course.router import router as course_router
from app.db.session import SessionLocal
from app.health.router import router as health_router
from app.users.router import router as users_router
from app.users.service import seed_admin


@asynccontextmanager
async def lifespan(_: FastAPI):
    with SessionLocal() as db:
        try:
            seed_admin(db)
            import_course_content(db)
            db.commit()
        except Exception:
            db.rollback()
            raise
    yield


app = FastAPI(
    title="MVP обучающей платформы",
    description="Учебный Python/FastAPI backend с авторизацией, курсом, прогрессом и сертификатом.",
    version="0.1.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allow_headers=["Authorization", "Content-Type"],
)

register_error_handlers(app)
app.include_router(auth_router)
app.include_router(users_router)
app.include_router(course_router)
app.include_router(certificate_router)
app.include_router(health_router)
