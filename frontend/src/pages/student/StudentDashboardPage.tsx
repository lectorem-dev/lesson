import axios from 'axios'
import { useEffect, useState } from 'react'
import { getCourse, getLessons } from '../../features/course/api/courseApi'
import type { CourseSummary, LessonListItem } from '../../features/course/model/courseTypes'
import { CourseProgressBar } from '../../features/course/ui/CourseProgressBar'
import { LessonList } from '../../features/course/ui/LessonList'
import { getAuthUser } from '../../features/auth/model/authStorage'
import { PageLayout } from '../../shared/ui/PageLayout'

function getErrorMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.message
    if (typeof message === 'string' && message.trim()) {
      return message
    }
  }

  return 'Не удалось загрузить курс.'
}

export function StudentDashboardPage() {
  const user = getAuthUser()
  const [course, setCourse] = useState<CourseSummary | null>(null)
  const [lessons, setLessons] = useState<LessonListItem[]>([])
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    let isMounted = true

    Promise.all([getCourse(), getLessons()])
      .then(([loadedCourse, loadedLessons]) => {
        if (isMounted) {
          setCourse(loadedCourse)
          setLessons(loadedLessons)
          setError('')
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

  return (
    <PageLayout title="Кабинет студента">
      {isLoading ? <p className="empty-state">Загрузка...</p> : null}
      {error ? <p className="form-error">{error}</p> : null}

      {!isLoading && !error && course ? (
        <div className="student-dashboard">
          <section className="course-summary">
            <p className="course-summary__user">Здравствуйте, {user?.name ?? 'студент'}.</p>
            <h2>{course.title}</h2>
            {course.description ? <p>{course.description}</p> : null}
          </section>

          <CourseProgressBar
            completedLessons={course.completedLessons}
            progressPercent={course.progressPercent}
            totalLessons={course.totalLessons}
          />

          <LessonList lessons={lessons} />
        </div>
      ) : null}
    </PageLayout>
  )
}
