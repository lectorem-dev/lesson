import { useNavigate } from 'react-router-dom'
import { Button } from '../../../shared/ui/Button'
import type { SubmitTestResponse } from '../model/courseTypes'

type TestResultProps = {
  onRetry: () => void
  result: SubmitTestResponse
}

export function TestResult({ onRetry, result }: TestResultProps) {
  const navigate = useNavigate()

  if (result.passed) {
    return (
      <section className="test-result test-result--passed">
        <p>
          Тест пройден. Ваш результат: {result.score} из {result.totalScore} баллов
          ({result.scorePercent}%).
        </p>
        {result.courseCompleted ? <p>Курс завершен.</p> : null}
        <div className="form-actions">
          {result.courseCompleted ? (
            <Button onClick={() => navigate('/student/completed')} type="button">
              Перейти на страницу завершения курса
            </Button>
          ) : null}
          {result.nextLessonId !== null ? (
            <Button onClick={() => navigate(`/student/lessons/${result.nextLessonId}`)} type="button">
              Перейти к следующему уроку
            </Button>
          ) : null}
          <Button onClick={() => navigate('/student')} type="button" variant="secondary">
            Вернуться к курсу
          </Button>
        </div>
      </section>
    )
  }

  return (
    <section className="test-result test-result--failed">
      <p>
        Тест не пройден. Ваш результат: {result.score} из {result.totalScore} баллов.
        Нужно минимум {result.passScore} баллов.
      </p>
      <Button onClick={onRetry} type="button" variant="secondary">
        Пройти заново
      </Button>
    </section>
  )
}
