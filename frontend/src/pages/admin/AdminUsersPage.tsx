import axios from 'axios'
import { useEffect, useMemo, useState } from 'react'
import { archiveUser, createUser, getUsers, updateUser } from '../../features/users/api/usersApi'
import type { User, UserCreateRequest, UserUpdateRequest } from '../../features/users/model/userTypes'
import { UserForm } from '../../features/users/ui/UserForm'
import { UsersTable } from '../../features/users/ui/UsersTable'
import { Button } from '../../shared/ui/Button'
import { Input } from '../../shared/ui/Input'
import { Modal } from '../../shared/ui/Modal'
import { PageLayout } from '../../shared/ui/PageLayout'

type FormState =
  | { mode: 'create'; user?: undefined }
  | { mode: 'edit'; user: User }

function getErrorMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.message
    if (typeof message === 'string' && message.trim()) {
      return message
    }
  }

  return 'Не удалось выполнить запрос.'
}

export function AdminUsersPage() {
  const [users, setUsers] = useState<User[]>([])
  const [search, setSearch] = useState('')
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [formState, setFormState] = useState<FormState | null>(null)
  const [archiveTarget, setArchiveTarget] = useState<User | null>(null)

  const filteredUsers = useMemo(() => {
    const query = search.trim().toLowerCase()

    if (!query) {
      return users
    }

    return users.filter((user) => user.name.toLowerCase().includes(query))
  }, [search, users])

  async function loadUsers() {
    setError('')
    setIsLoading(true)

    try {
      setUsers(await getUsers())
    } catch (loadError) {
      setError(getErrorMessage(loadError))
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    let isMounted = true

    getUsers()
      .then((loadedUsers) => {
        if (isMounted) {
          setError('')
          setUsers(loadedUsers)
        }
      })
      .catch((loadError: unknown) => {
        if (isMounted) {
          setError(getErrorMessage(loadError))
        }
      })
      .finally(() => {
        if (isMounted) {
          setIsLoading(false)
        }
      })

    return () => {
      isMounted = false
    }
  }, [])

  async function handleSubmit(request: UserCreateRequest | UserUpdateRequest) {
    setError('')
    setIsSubmitting(true)

    try {
      if (formState?.mode === 'edit') {
        await updateUser(formState.user.id, request as UserUpdateRequest)
      } else {
        await createUser(request as UserCreateRequest)
      }

      setFormState(null)
      await loadUsers()
    } catch (submitError) {
      setError(getErrorMessage(submitError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleArchiveConfirm() {
    if (!archiveTarget) {
      return
    }
    setError('')
    setIsSubmitting(true)

    try {
      await archiveUser(archiveTarget.id)
      setArchiveTarget(null)
      await loadUsers()
    } catch (archiveError) {
      setError(getErrorMessage(archiveError))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <PageLayout
      actions={
        <Button onClick={() => setFormState({ mode: 'create' })} type="button">
          Добавить пользователя
        </Button>
      }
      title="Пользователи"
    >
      <section className="toolbar">
        <Input
          label="Поиск по имени"
          name="search"
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Введите имя"
          value={search}
        />
      </section>

      {error ? <p className="form-error">{error}</p> : null}
      {isLoading ? (
        <p className="empty-state">Загрузка...</p>
      ) : (
        <UsersTable
          onArchive={(user) => setArchiveTarget(user)}
          onEdit={(user) => setFormState({ mode: 'edit', user })}
          users={filteredUsers}
        />
      )}

      {formState ? (
        <Modal
          onClose={() => setFormState(null)}
          title={formState.mode === 'create' ? 'Добавить пользователя' : 'Редактировать пользователя'}
        >
          <UserForm
            isSubmitting={isSubmitting}
            mode={formState.mode}
            onCancel={() => setFormState(null)}
            onSubmit={handleSubmit}
            user={formState.mode === 'edit' ? formState.user : undefined}
          />
        </Modal>
      ) : null}

      {archiveTarget ? (
        <Modal onClose={() => setArchiveTarget(null)} title="Перенос в архив">
          <div className="confirm-dialog">
            <p>
              Уверены, что хотите перенести пользователя «{archiveTarget.name}» в архив?
            </p>
            <div className="form-actions">
              <Button disabled={isSubmitting} onClick={handleArchiveConfirm} type="button" variant="danger">
                {isSubmitting ? 'Перенос...' : 'Перенести в архив'}
              </Button>
              <Button disabled={isSubmitting} onClick={() => setArchiveTarget(null)} type="button" variant="secondary">
                Отмена
              </Button>
            </div>
          </div>
        </Modal>
      ) : null}
    </PageLayout>
  )
}
