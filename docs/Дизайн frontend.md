# Frontend дизайн

## Стек

- React
- TypeScript
- Vite
- React Router
- Axios
- react-markdown

---

## Структура проекта

Ключевая структура:

```text
src/
  app/          # App и маршруты
  pages/        # страницы приложения
  features/     # бизнес-модули: auth, users, course, certificate
  shared/       # общие API, config, стили, UI-компоненты
  main.tsx
```

Правила размещения:

- `pages/*` — только сборка страницы из готовых компонентов.
- `features/*/api` — запросы к backend.
- `features/*/model` — TypeScript-типы и модельные утилиты.
- `features/*/ui` — компоненты конкретной фичи.
- `shared/ui` — переиспользуемые базовые компоненты.
- `shared/api/httpClient.ts` — единый axios-клиент.
- `shared/styles` — глобальные стили, переменные, шрифты.

---

## Общие компоненты

Использовать и развивать существующие компоненты:

```text
shared/ui/
  Button.tsx
  Input.tsx
  Select.tsx
  Modal.tsx
  PageLayout.tsx
```

Если нужен новый общий элемент, добавлять его в `shared/ui` только если он реально переиспользуется минимум в двух
местах.

Компоненты должны быть простыми, типизированными и без скрытой бизнес-логики.

---

## Авторизация

JWT хранить через существующий `authStorage`.

Защищенные маршруты делать через:

```text
features/auth/ui/ProtectedRoute.tsx
features/auth/ui/RoleRedirect.tsx
```

При 401 очищать authStorage и отправлять пользователя на `/login`.

---

## Визуальный стиль

Стиль: темная неоновая технологическая платформа по мотивам изображения `techfluency.png`.

Ассоциации:

- ночной город;
- фиолетовая и синяя подсветка;
- цифровые линии;
- стеклянные карточки;
- легкое свечение;
- учебная IT-платформа.

Основная палитра:

```css
:root {
    /* Основной фон приложения */
    --color-bg: #07091f;

    /* Полупрозрачные стеклянные поверхности */
    --color-surface: rgba(18, 22, 56, 0.78);
    --color-surface-strong: rgba(28, 34, 82, 0.92);

    /* Основные акцентные цвета */
    --color-primary: #9b5cff;     /* неоновый фиолетовый */
    --color-secondary: #20d9ff;  /* яркий голубой */
    --color-accent: #ff4fd8;     /* розово-пурпурный */

    /* Текст */
    --color-text: #f4f7ff;
    --color-muted: #aab3d8;

    /* Границы и разделители */
    --color-border: rgba(135, 155, 255, 0.28);

    /* Служебные цвета */
    --color-danger: #ff5c7a;
    --color-success: #41f0a2;
}
```

Фон:

```css
body {
    background: radial-gradient(circle at 20% 10%, rgba(155, 92, 255, 0.28), transparent 32%),
    radial-gradient(circle at 80% 20%, rgba(32, 217, 255, 0.18), transparent 34%),
    linear-gradient(135deg, #050716 0%, #090d2a 48%, #12071f 100%);
}
```

Карточки:

```css
.card {
    background: rgba(18, 22, 56, 0.78);
    border: 1px solid rgba(135, 155, 255, 0.28);
    border-radius: 20px;
    box-shadow: 0 0 32px rgba(32, 217, 255, 0.12);
    backdrop-filter: blur(16px);
}
```

Кнопки:

```css
.button-primary {
    background: linear-gradient(135deg, #9b5cff, #20d9ff);
    color: #ffffff;
    border: none;
    box-shadow: 0 0 22px rgba(155, 92, 255, 0.35);
}
```

---

## Шрифты

Использовать локальные шрифты из `public/fonts`.

Основные:

```css
@font-face {
    font-family: "Zen Dots";
    src: url("/fonts/ZenDots-Regular.ttf") format("truetype");
    font-weight: 400;
}

@font-face {
    font-family: "Montserrat";
    src: url("/fonts/Montserrat-Regular.ttf") format("truetype");
    font-weight: 400;
}

@font-face {
    font-family: "Montserrat";
    src: url("/fonts/Montserrat-SemiBold.ttf") format("truetype");
    font-weight: 600;
}
```

Правила:

- `Zen Dots` — логотипы, крупные заголовки, акценты.
- `Montserrat` — весь основной интерфейс и текст.

---

## CSS-правила

- Глобальные переменные хранить в `shared/styles/variables.css`.
- Базовые стили хранить в `shared/styles/global.css`.
- Не писать большие inline-style.
- Не дублировать цвета вручную, использовать CSS-переменные.
- Компоненты должны выглядеть согласованно.

---

## Страницы

Основные страницы:

```text
/
  Главная страница с кнопкой входа.

/login
  Форма авторизации.

/admin/users
  Таблица пользователей, поиск, создание, редактирование, архивирование.

/student
  Кабинет студента, прогресс, список уроков.

/student/lessons/:lessonId
  Markdown-урок и тест.

/student/completed
  Поздравление и скачивание сертификата.
```

---

## UX-правила

- Все тексты интерфейса на русском.
- Ошибки показывать текстом на странице, не через `alert`.
- Загрузку показывать явно.
- Заблокированные действия делать `disabled`.
- После успешного действия обновлять данные с backend.
- Не скрывать ошибку backend, а показывать понятное сообщение.
