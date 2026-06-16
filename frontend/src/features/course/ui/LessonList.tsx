import { useNavigate } from 'react-router-dom'
import { Button } from '../../../shared/ui/Button'
import type { LessonListItem } from '../model/courseTypes'
import { LessonStatusBadge } from './LessonStatusBadge'

type LessonListProps = {
  lessons: LessonListItem[]
}

export function LessonList({ lessons }: LessonListProps) {
  const navigate = useNavigate()
  const sortedLessons = [...lessons].sort((left, right) => left.position - right.position)

  if (sortedLessons.length === 0) {
    return <p className="empty-state">Уроки пока не добавлены.</p>
  }

  return (
    <section className="lesson-list" aria-label="Список уроков">
      {sortedLessons.map((lesson) => (
        <article className="lesson-row" key={lesson.id}>
          <div className="lesson-row__number">{lesson.position}</div>
          <div className="lesson-row__body">
            <div className="lesson-row__main">
              <h3>{lesson.title}</h3>
              <LessonStatusBadge status={lesson.status} />
            </div>
            <div className="lesson-row__meta">
              {lesson.status === 'PASSED' ? <span>Пройден</span> : null}
              {lesson.bestScorePercent !== null ? (
                <span>Лучший балл: {lesson.bestScorePercent}%</span>
              ) : null}
              {lesson.locked ? (
                <span>Откроется после прохождения предыдущего урока</span>
              ) : null}
            </div>
          </div>
          <Button
            disabled={lesson.locked}
            onClick={() => navigate(`/student/lessons/${lesson.id}`)}
            type="button"
            variant={lesson.locked ? 'secondary' : 'primary'}
          >
            Открыть
          </Button>
        </article>
      ))}
    </section>
  )
}
