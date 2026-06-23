import axios from 'axios'
import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { login } from '../../features/auth/api/authApi'
import { saveAuth } from '../../features/auth/model/authStorage'
import { Button } from '../../shared/ui/Button'
import { Input } from '../../shared/ui/Input'

export function LoginPage() {
  const navigate = useNavigate()
  const [loginValue, setLoginValue] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    if (!loginValue.trim() || !password.trim()) {
      setError('Введите логин и пароль.')
      return
    }

    try {
      setIsSubmitting(true)
      const response = await login({ login: loginValue.trim(), password })
      saveAuth(response)
      navigate(response.role === 'ADMIN' ? '/admin/users' : '/student', { replace: true })
    } catch (requestError) {
      if (axios.isAxiosError(requestError) && requestError.response?.status === 403) {
        setError('Пользователь архивирован.')
      } else {
        setError('Неверный логин или пароль.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <form className="auth-form" onSubmit={handleSubmit}>
        <h1>Вход</h1>
        {error ? <p className="form-error">{error}</p> : null}
        <Input
          autoComplete="username"
          label="Логин"
          name="login"
          onChange={(event) => setLoginValue(event.target.value)}
          value={loginValue}
        />
        <Input
          autoComplete="current-password"
          label="Пароль"
          name="password"
          onChange={(event) => setPassword(event.target.value)}
          type="password"
          value={password}
        />
        <Button disabled={isSubmitting} type="submit">
          {isSubmitting ? 'Вход...' : 'Войти'}
        </Button>
        <Button onClick={() => navigate('/')} type="button" variant="secondary">
          Назад
        </Button>
      </form>
    </main>
  )
}
