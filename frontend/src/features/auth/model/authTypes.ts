import type { Role } from '../../users/model/userTypes'

export type LoginRequest = {
  login: string
  password: string
}

export type LoginResponse = {
  token: string
  userId: string
  login: string
  name: string
  role: Role
}

export type AuthUser = {
  userId: string
  login: string
  name: string
  role: Role
}
