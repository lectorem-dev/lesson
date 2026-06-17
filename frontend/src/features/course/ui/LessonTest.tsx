import { type FormEvent, useMemo, useState } from 'react'
import { Button } from '../../../shared/ui/Button'
import type { LessonTest as LessonTestType } from '../model/courseTypes'

type SelectedAnswers = Record<string, string>

type LessonTestProps = {
  error: string
  isSubmitting: boolean
  onSubmit: (answers: SelectedAnswers) => Promise<void>
  test: LessonTestType
}

export function LessonTest({ error, isSubmitting, onSubmit, test }: LessonTestProps) {
  const [selectedAnswers, setSelectedAnswers] = useState<SelectedAnswers>({})

  // Пока не выбран ответ в каждом вопросе, отправку блокируем, чтобы backend получал полный набор ответов.
  const allQuestionsAnswered = useMemo(
    () => test.questions.every((question) => selectedAnswers[question.id] !== undefined),
    [selectedAnswers, test.questions],
  )

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    await onSubmit(selectedAnswers)
  }

  return (
    <section className="lesson-test">
      <div className="lesson-test__header">
        <h2>Тест</h2>
        <span>Минимум для прохождения: {test.passPercent}%</span>
      </div>

      {error ? <p className="form-error">{error}</p> : null}

      <form className="test-form" onSubmit={handleSubmit}>
        {test.questions.map((question, questionIndex) => (
          <fieldset className="test-question" key={question.id}>
            <legend>
              {questionIndex + 1}. {question.text}
            </legend>
            <div className="test-answers">
              {question.answers.map((answer) => (
                <label className="test-answer" key={answer.id}>
                  <input
                    checked={selectedAnswers[question.id] === answer.id}
                    name={`question-${question.id}`}
                    onChange={() =>
                      setSelectedAnswers((current) => ({
                        ...current,
                        [question.id]: answer.id,
                      }))
                    }
                    type="radio"
                    value={answer.id}
                  />
                  <span>{answer.text}</span>
                </label>
              ))}
            </div>
          </fieldset>
        ))}

        <Button disabled={!allQuestionsAnswered || isSubmitting} type="submit">
          {isSubmitting ? 'Проверка...' : 'Проверить'}
        </Button>
      </form>
    </section>
  )
}
