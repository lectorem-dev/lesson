import { type FormEvent, useState } from 'react'
import { Button } from '../../../shared/ui/Button'
import { Input } from '../../../shared/ui/Input'
import { Modal } from '../../../shared/ui/Modal'
import { Select } from '../../../shared/ui/Select'
import {
  ROLE_LABEL,
  USER_STATUS_LABEL,
  type Role,
  type User,
  type UserCreateRequest,
  type UserStatus,
  type UserUpdateRequest,
} from '../model/userTypes'

type UserFormProps = {
  isSubmitting: boolean
  mode: 'create' | 'edit'
  onCancel: () => void
  onSubmit: (request: UserCreateRequest | UserUpdateRequest) => Promise<void>
  user?: User
}

type FormErrors = Partial<Record<'login' | 'name' | 'password' | 'role' | 'status', string>>

const CYRILLIC_TO_LATIN: Record<string, string> = {
  а: 'a',
  б: 'b',
  в: 'v',
  г: 'g',
  д: 'd',
  е: 'e',
  ё: 'e',
  ж: 'zh',
  з: 'z',
  и: 'i',
  й: 'i',
  к: 'k',
  л: 'l',
  м: 'm',
  н: 'n',
  о: 'o',
  п: 'p',
  р: 'r',
  с: 's',
  т: 't',
  у: 'u',
  ф: 'f',
  х: 'kh',
  ц: 'ts',
  ч: 'ch',
  ш: 'sh',
  щ: 'shch',
  ъ: '',
  ы: 'y',
  ь: '',
  э: 'e',
  ю: 'yu',
  я: 'ya',
}

function transliterate(value: string) {
  return value
    .trim()
    .toLowerCase()
    .split('')
    .map((letter) => CYRILLIC_TO_LATIN[letter] ?? letter)
    .join('')
    .replace(/[^a-z0-9]/g, '')
}

function capitalize(value: string) {
  if (!value) {
    return ''
  }

  return value[0].toUpperCase() + value.slice(1)
}

function buildLoginFromName(value: string) {
  const parts = value.trim().split(/\s+/).filter(Boolean)
  const surname = capitalize(transliterate(parts[0] ?? ''))
  const initials = parts
    .slice(1, 3)
    .map((part) => transliterate(part)[0]?.toUpperCase() ?? '')
    .join('')

  return `${surname}${initials}`
}

export function UserForm({ isSubmitting, mode, onCancel, onSubmit, user }: UserFormProps) {
  const [name, setName] = useState(user?.name ?? '')
  const [login, setLogin] = useState(user?.login ?? '')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState<Role>(user?.role ?? 'STUDENT')
  const [status, setStatus] = useState<UserStatus>(user?.status ?? 'ACTIVE')
  const [errors, setErrors] = useState<FormErrors>({})
  const [isLoginHelpOpen, setIsLoginHelpOpen] = useState(false)

  function handleNameChange(value: string) {
    setName(value)
    setLogin(buildLoginFromName(value))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const nextErrors: FormErrors = {}
    if (!name.trim()) {
      nextErrors.name = 'Введите имя.'
    }
    if (!login.trim()) {
      nextErrors.login = 'Введите логин.'
    }
    if (mode === 'create' && !password.trim()) {
      nextErrors.password = 'Введите пароль.'
    }

    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) {
      return
    }

    if (mode === 'create') {
      await onSubmit({
        name: name.trim(),
        login: login.trim(),
        password,
        role,
      })
      return
    }

    await onSubmit({
      name: name.trim(),
      login: login.trim(),
      password: password.trim() ? password : null,
      role,
      status,
    })
  }

  return (
    <>
      <form className="user-form" onSubmit={handleSubmit}>
        <Input
          error={errors.name}
          label="Имя"
          name="name"
          onChange={(event) => handleNameChange(event.target.value)}
          value={name}
        />
        <label className="field" htmlFor="login">
          <span className="field__label field__label--with-action">
            Логин
            <button
              aria-label="Подсказка по формированию логина"
              className="login-help-button"
              onClick={() => setIsLoginHelpOpen(true)}
              type="button"
            >
              ?
            </button>
          </span>
          <input
            className="input"
            id="login"
            name="login"
            onChange={(event) => setLogin(event.target.value)}
            value={login}
          />
          {errors.login ? <span className="field__error">{errors.login}</span> : null}
        </label>
        <Input
          autoComplete="new-password"
          error={errors.password}
          label={mode === 'create' ? 'Пароль' : 'Новый пароль'}
          name="password"
          onChange={(event) => setPassword(event.target.value)}
          type="password"
          value={password}
        />
        <Select
          error={errors.role}
          label="Роль"
          name="role"
          onChange={(event) => setRole(event.target.value as Role)}
          value={role}
        >
          <option value="STUDENT">{ROLE_LABEL.STUDENT}</option>
          <option value="ADMIN">{ROLE_LABEL.ADMIN}</option>
        </Select>
        {mode === 'edit' ? (
          <Select
            error={errors.status}
            label="Статус"
            name="status"
            onChange={(event) => setStatus(event.target.value as UserStatus)}
            value={status}
          >
            <option value="ACTIVE">{USER_STATUS_LABEL.ACTIVE}</option>
            <option value="ARCHIVED">{USER_STATUS_LABEL.ARCHIVED}</option>
          </Select>
        ) : null}
        <div className="form-actions">
          <Button disabled={isSubmitting} type="submit">
            {isSubmitting ? 'Сохранение...' : 'Сохранить'}
          </Button>
          <Button onClick={onCancel} type="button" variant="secondary">
            Отмена
          </Button>
        </div>
      </form>

      {isLoginHelpOpen ? (
        <Modal onClose={() => setIsLoginHelpOpen(false)} title="Как формировать логин">
          <div className="confirm-dialog">
            <p>
              Логин формируется из фамилии латиницей и первых букв имени и отчества. Если такой
              логин уже занят, добавьте короткий отличительный суффикс или цифру.
            </p>
            <p>Пример: Иванов Иван Иванович — IvanovII.</p>
            <div className="form-actions">
              <Button onClick={() => setIsLoginHelpOpen(false)} type="button">
                Понятно
              </Button>
            </div>
          </div>
        </Modal>
      ) : null}
    </>
  )
}
