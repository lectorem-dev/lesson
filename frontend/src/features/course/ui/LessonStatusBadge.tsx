import type { LessonStatus } from '../model/courseTypes'

type LessonStatusBadgeProps = {
  status: LessonStatus
}

const STATUS_LABEL: Record<LessonStatus, string> = {
  AVAILABLE: 'Доступен',
  LOCKED: 'Закрыт',
  PASSED: 'Пройден',
}

export function LessonStatusBadge({ status }: LessonStatusBadgeProps) {
  return (
    <span className={`lesson-status lesson-status--${status.toLowerCase()}`}>
      {STATUS_LABEL[status]}
    </span>
  )
}
