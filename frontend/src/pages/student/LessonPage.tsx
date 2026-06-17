import axios from 'axios'
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { getLessonById, submitLessonTest } from '../../features/course/api/courseApi'
import type { LessonDetails, SubmitTestResponse } from '../../features/course/model/courseTypes'
import { LessonTest } from '../../features/course/ui/LessonTest'
import { MarkdownLessonContent } from '../../features/course/ui/MarkdownLessonContent'
import { TestResult } from '../../features/course/ui/TestResult'
import { Button } from '../../shared/ui/Button'
import { PageLayout } from '../../shared/ui/PageLayout'

type LessonErrorKind = 'forbidden' | 'not-found' | 'common'

type LessonError = {
  kind: LessonErrorKind
  message: string
}

type SelectedAnswers = Record<string, string>

function parseLessonId(value: string | undefined) {
  const normalized = value?.trim()
  if (!normalized) {
    return null
  }

  return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(
    normalized,
  )
    ? normalized
    : null
}

function getLoadError(error: unknown): LessonError {
  if (axios.isAxiosError(error)) {
    if (error.response?.status === 403) {
      return { kind: 'forbidden', message: 'Урок пока недоступен' }
    }

    if (error.response?.status === 404) {
      return { kind: 'not-found', message: 'Урок не найден' }
    }

    const message = error.response?.data?.message
    if (typeof message === 'string' && message.trim()) {
      return { kind: 'common', message }
    }
  }

  return { kind: 'common', message: 'Не удалось загрузить урок.' }
}

function getSubmitError(error: unknown) {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.message
    if (typeof message === 'string' && message.trim()) {
      return message
    }
  }

  return 'Не удалось проверить тест.'
}

export function LessonPage() {
  const { lessonId: lessonIdParam } = useParams()

  // При смене lessonId перемонтируем страницу урока, чтобы сбросить локальное состояние предыдущего урока.
  return <LessonPageContent key={lessonIdParam ?? 'invalid'} lessonIdParam={lessonIdParam} />
}

type LessonPageContentProps = {
  lessonIdParam?: string
}

function LessonPageContent({ lessonIdParam }: LessonPageContentProps) {
  const navigate = useNavigate()
  const lessonId = parseLessonId(lessonIdParam)
  const [lesson, setLesson] = useState<LessonDetails | null>(null)
  const [loadError, setLoadError] = useState<LessonError | null>(null)
  const [submitError, setSubmitError] = useState('')
  const [result, setResult] = useState<SubmitTestResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    if (lessonId === null) {
      return
    }

    let isMounted = true

    getLessonById(lessonId)
      .then((loadedLesson) => {
        if (isMounted) {
          setLesson(loadedLesson)
          setLoadError(null)
        }
      })
      .catch((error: unknown) => {
        if (isMounted) {
          setLesson(null)
          setLoadError(getLoadError(error))
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
  }, [lessonId, reloadKey])

  async function handleSubmit(selectedAnswers: SelectedAnswers) {
    if (!lesson || lessonId === null) {
      return
    }

    setSubmitError('')
    setIsSubmitting(true)

    try {
      const response = await submitLessonTest(lessonId, {
        answers: lesson.test.questions.map((question) => ({
          questionId: question.id,
          answerId: selectedAnswers[question.id],
        })),
      })
      setResult(response)
    } catch (error) {
      setSubmitError(getSubmitError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleRetry() {
    // Повторная загрузка урока нужна и для сброса результата, и для нового перемешивания ответов.
    setResult(null)
    setSubmitError('')
    setLoadError(null)
    setLesson(null)
    setIsLoading(true)
    setReloadKey((current) => current + 1)
  }

  if (lessonId === null) {
    return (
      <PageLayout title="Урок">
        <section className="lesson-error">
          <p>Урок не найден</p>
        </section>
      </PageLayout>
    )
  }

  const isStaleLesson = lesson !== null && lesson.id !== lessonId

  if (isLoading || isStaleLesson) {
    return (
      <PageLayout title="Урок">
        <p className="empty-state">Загрузка...</p>
      </PageLayout>
    )
  }

  if (loadError) {
    return (
      <PageLayout title="Урок">
        <section className="lesson-error">
          <p>{loadError.message}</p>
          {loadError.kind !== 'not-found' ? (
            <Button onClick={() => navigate('/student')} type="button" variant="secondary">
              Назад к курсу
            </Button>
          ) : null}
        </section>
      </PageLayout>
    )
  }

  if (!lesson) {
    return null
  }

  return (
    <PageLayout
      actions={
        <Button onClick={() => navigate('/student')} type="button" variant="secondary">
          Назад к курсу
        </Button>
      }
      title={lesson.title}
    >
      <div className="lesson-page">
        <MarkdownLessonContent markdownContent={lesson.markdownContent} />

        {result ? (
          <TestResult onRetry={handleRetry} result={result} />
        ) : (
          <LessonTest
            error={submitError}
            isSubmitting={isSubmitting}
            onSubmit={handleSubmit}
            test={lesson.test}
            key={lesson.id}
          />
        )}
      </div>
    </PageLayout>
  )
}
