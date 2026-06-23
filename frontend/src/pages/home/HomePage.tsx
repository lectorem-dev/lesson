import { Link } from 'react-router-dom'

export function HomePage() {
  return (
    <div className="home-page">
      <header className="home-header">
        <Link className="home-header__brand" to="/">
          Техноварк
        </Link>
        <Link aria-label="Перейти ко входу в профиль" className="home-header__profile-link" to="/login">
          <svg aria-hidden="true" className="home-header__profile-icon" viewBox="0 0 24 24">
            <path d="M12 12.5a4.2 4.2 0 1 0 0-8.4 4.2 4.2 0 0 0 0 8.4Z" />
            <path d="M4.8 20.2c.8-3.4 3.5-5.4 7.2-5.4s6.4 2 7.2 5.4" />
          </svg>
        </Link>
      </header>

      <main className="home-main">
        <section className="home-hero-media" aria-label="Обложка курса">
          <img src="/techfluency.png" alt="Tech Fluency" />
        </section>

        <section className="home-hero-title home-card" aria-labelledby="course-title">
          <h1 id="course-title">Tech Fluency: английский язык для IT-специалистов</h1>
          <p>
            Практический курс английского языка для работы с документацией, интерфейсами, API и
            профессиональной коммуникацией в IT-среде.
          </p>
        </section>

        <section className="home-info-grid" aria-label="Описание курса">
          <article className="home-card">
            <h2>О чем курс</h2>
            <p>
              Курс помогает освоить базовую IT-лексику, научиться читать техническую документацию,
              понимать сообщения интерфейсов, работать с англоязычными материалами и увереннее
              ориентироваться в профессиональной среде.
            </p>
          </article>

          <article className="home-card">
            <h2>Что получит студент</h2>
            <p>
              Студент изучит ключевые термины из frontend, backend и разработки программных систем,
              пройдет короткие уроки с проверочными тестами и закрепит материал через
              последовательное прохождение курса.
            </p>
          </article>
        </section>

        <section className="home-author home-card" aria-labelledby="author-title">
          <div className="home-author__photo" aria-label="Фото автора">
            Фото
          </div>
          <div className="home-author__content">
            <h2 id="author-title">Автор курса</h2>
            <h3>Иванов Алексей Сергеевич</h3>
            <p>
              Преподаватель английского языка для IT-направлений. Специализируется на
              профессиональной лексике, чтении технической документации и подготовке студентов к
              работе с англоязычными цифровыми продуктами.
            </p>
          </div>
        </section>
      </main>

      <footer className="home-footer">
        <p>© 2026 Техноварк</p>
        <p>Образовательная демонстрационная платформа</p>
        <p>Контакты: info@technowark.example</p>
        <p>Материалы предназначены для учебного использования</p>
      </footer>
    </div>
  )
}
