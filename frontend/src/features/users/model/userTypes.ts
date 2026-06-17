export type Role = 'ADMIN' | 'STUDENT'

export type UserStatus = 'ACTIVE' | 'ARCHIVED'

export const ROLE_LABEL: Record<Role, string> = {
  ADMIN: 'Администратор',
  STUDENT: 'Студент',
}

export const USER_STATUS_LABEL: Record<UserStatus, string> = {
  ACTIVE: 'Активен',
  ARCHIVED: 'В архиве',
}

export type User = {
  id: string
  name: string
  login: string
  role: Role
  status: UserStatus
  createdAt: string
  updatedAt: string
}

export type UserCreateRequest = {
  name: string
  login: string
  password: string
  role: Role
}

export type UserUpdateRequest = {
  name: string
  login: string
  password?: string | null
  role: Role
  status: UserStatus
}
