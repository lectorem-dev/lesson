import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { clearAuth, getAuthUser } from '../../features/auth/model/authStorage'
import { ROLE_LABEL } from '../../features/users/model/userTypes'
import { Button } from './Button'

type PageLayoutProps = {
  actions?: ReactNode
  children: ReactNode
  title?: string
}

export function PageLayout({ actions, children, title }: PageLayoutProps) {
  const navigate = useNavigate()
  const user = getAuthUser()

  function handleLogout() {
    clearAuth()
    navigate('/', { replace: true })
  }

  return (
    <div className="page-shell">
      <header className="page-header">
        <div>
          {title ? <h1>{title}</h1> : null}
          {user ? (
            <p className="page-header__meta">
              {user.name} · {ROLE_LABEL[user.role]}
            </p>
          ) : null}
        </div>
        <div className="page-header__actions">
          {actions}
          {user ? (
            <Button onClick={handleLogout} type="button" variant="secondary">
              Выйти
            </Button>
          ) : null}
        </div>
      </header>
      <main>{children}</main>
    </div>
  )
}
