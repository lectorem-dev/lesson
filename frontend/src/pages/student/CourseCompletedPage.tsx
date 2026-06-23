import axios from 'axios'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { downloadCertificate } from '../../features/certificate/api/certificateApi'
import { getProgress } from '../../features/course/api/courseApi'
import type { CourseProgress } from '../../features/course/model/courseTypes'
import { Button } from '../../shared/ui/Button'
import { PageLayout } from '../../shared/ui/PageLayout'

function getErrorMessage(error: unknown, fallback: string) {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.message
    if (typeof message === 'string' && message.trim()) {
      return message
    }
  }

  return fallback
}

export function CourseCompletedPage() {
  const navigate = useNavigate()
  const [progress, setProgress] = useState<CourseProgress | null>(null)
  const [loadError, setLoadError] = useState('')
  const [downloadError, setDownloadError] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isDownloading, setIsDownloading] = useState(false)

  useEffect(() => {
    let isMounted = true

    getProgress()
      .then((loadedProgress) => {
        if (isMounted) {
          setProgress(loadedProgress)
          setLoadError('')
        }
      })
      .catch((error: unknown) => {
        if (isMounted) {
          setLoadError(getErrorMessage(error, 'Не удалось загрузить прогресс курса.'))
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

  async function handleDownload() {
    setDownloadError('')
    setIsDownloading(true)

    try {
      await downloadCertificate()
    } catch (error) {
      setDownloadError(getErrorMessage(error, 'Не удалось скачать сертификат.'))
    } finally {
      setIsDownloading(false)
    }
  }

  return (
    <PageLayout title="Поздравляем!">
      {isLoading ? <p className="empty-state">Загрузка...</p> : null}
      {loadError ? <p className="form-error">{loadError}</p> : null}

      {!isLoading && !loadError && progress ? (
        <section className="course-completed">
          {progress.courseCompleted ? (
            <>
              <div className="course-completed__mark" aria-hidden="true">
                ✓
              </div>
              <div className="course-completed__content">
                <h2>Курс успешно завершен</h2>
                <p>
                  Вы прошли все уроки Tech Fluency и можете скачать сертификат, подтверждающий
                  завершение курса.
                </p>
              </div>
              {downloadError ? <p className="form-error">{downloadError}</p> : null}
              <div className="form-actions course-completed__actions">
                <Button disabled={isDownloading} onClick={handleDownload} type="button">
                  {isDownloading ? 'Скачивание...' : 'Скачать сертификат'}
                </Button>
                <Button onClick={() => navigate('/student')} type="button" variant="secondary">
                  Вернуться к курсу
                </Button>
              </div>
            </>
          ) : (
            <>
              <div className="course-completed__mark course-completed__mark--pending" aria-hidden="true">
                …
              </div>
              <div className="course-completed__content">
                <h2>Курс еще не завершен</h2>
                <p>Завершите все доступные уроки, чтобы открыть сертификат.</p>
              </div>
              <div className="form-actions course-completed__actions">
                <Button onClick={() => navigate('/student')} type="button" variant="secondary">
                  Вернуться к курсу
                </Button>
              </div>
            </>
          )}
        </section>
      ) : null}
    </PageLayout>
  )
}
