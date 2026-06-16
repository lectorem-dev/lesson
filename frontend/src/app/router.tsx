import { createBrowserRouter, Navigate } from 'react-router-dom'
import { ProtectedRoute } from '../features/auth/ui/ProtectedRoute'
import { RoleRedirect } from '../features/auth/ui/RoleRedirect'
import { AdminUsersPage } from '../pages/admin/AdminUsersPage'
import { HomePage } from '../pages/home/HomePage'
import { LoginPage } from '../pages/login/LoginPage'
import { LessonPage } from '../pages/student/LessonPage'
import { StudentDashboardPage } from '../pages/student/StudentDashboardPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <HomePage />,
  },
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/admin/users',
    element: (
      <ProtectedRoute allowedRole="ADMIN">
        <AdminUsersPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/student',
    element: (
      <ProtectedRoute>
        <StudentDashboardPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/student/lessons/:lessonId',
    element: (
      <ProtectedRoute>
        <LessonPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/redirect',
    element: <RoleRedirect />,
  },
  {
    path: '*',
    element: <Navigate to="/" replace />,
  },
])
