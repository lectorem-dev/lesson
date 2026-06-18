import { useNavigate } from 'react-router-dom'
import { Button } from '../../shared/ui/Button'
import './HomePage.css' // Создайте этот файл для стилей (код ниже)

export function HomePage() {
    const navigate = useNavigate()

    return (
        <main className="home-page">
            {/* Фоновое изображение */}
            <div className="home-bg" />

            {/* Навигация */}
            <header className="home-header">
                <div className="logo">TECHFLUENCY</div>
                <nav className="nav-links">
                    <a href="#about">О курсе</a>
                    <a href="#exam">Экзамен</a>
                    <a href="#contacts">Контакты</a>
                </nav>
                <Button onClick={() => navigate('/login')} type="button" className="btn-login">
                    Войти
                </Button>
            </header>

            {/* Основной контент */}
            <section className="hero-section">
                <h1 className="hero-title">TECHFLUENCY</h1>
                <p className="hero-subtitle">
                    Освойте Python, Java и веб-разработку в интерактивном формате.
                    Ваш путь к IT-карьере начинается здесь.
                </p>
                <div className="hero-actions">
                    <Button onClick={() => navigate('/login')} type="button" className="btn-primary">
                        Начать обучение
                    </Button>
                    <Button onClick={() => navigate('/login')} type="button" variant="outline" className="btn-secondary">
                        Подробнее о курсе
                    </Button>
                </div>
            </section>

            {/* Индикатор прокрутки */}
            <div className="scroll-indicator">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M6 9l6 6 6-6" />
                </svg>
            </div>
        </main>
    )
}