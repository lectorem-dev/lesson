export type CourseSummary = {
  id: string
  title: string
  description: string | null
  progressPercent: number
  completedLessons: number
  totalLessons: number
}

export type LessonStatus = 'LOCKED' | 'AVAILABLE' | 'PASSED'

export type LessonListItem = {
  id: string
  position: number
  title: string
  status: LessonStatus
  locked: boolean
  bestScorePercent: number | null
}

export type LessonDetails = {
  id: string
  position: number
  title: string
  markdownContent: string
  test: LessonTest
}

export type LessonTest = {
  passPercent: number
  questions: TestQuestion[]
}

export type TestQuestion = {
  id: string
  text: string
  answers: TestAnswer[]
}

export type TestAnswer = {
  id: string
  text: string
}

export type SubmitAnswer = {
  questionId: string
  answerId: string
}

export type SubmitTestRequest = {
  answers: SubmitAnswer[]
}

export type SubmitTestResponse = {
  passed: boolean
  scorePercent: number
  passPercent: number
  nextLessonId: string | null
  courseCompleted: boolean
}

export type CourseProgress = {
  completedLessons: number
  totalLessons: number
  progressPercent: number
  courseCompleted: boolean
}
