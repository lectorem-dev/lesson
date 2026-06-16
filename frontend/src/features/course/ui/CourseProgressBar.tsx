type CourseProgressBarProps = {
  completedLessons: number
  totalLessons: number
  progressPercent: number
}

export function CourseProgressBar({
  completedLessons,
  progressPercent,
  totalLessons,
}: CourseProgressBarProps) {
  const normalizedPercent = Math.max(0, Math.min(100, progressPercent))

  return (
    <section className="course-progress" aria-label="Прогресс курса">
      <div className="course-progress__header">
        <span>
          Пройдено {completedLessons} из {totalLessons} уроков
        </span>
        <strong>{normalizedPercent}%</strong>
      </div>
      <div className="course-progress__track" aria-hidden="true">
        <div className="course-progress__fill" style={{ width: `${normalizedPercent}%` }} />
      </div>
    </section>
  )
}
