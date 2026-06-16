export type CourseSummary = {
  id: number
  title: string
  description: string | null
  progressPercent: number
  completedLessons: number
  totalLessons: number
}

export type LessonStatus = 'LOCKED' | 'AVAILABLE' | 'PASSED'

export type LessonListItem = {
  id: number
  position: number
  title: string
  status: LessonStatus
  locked: boolean
  bestScorePercent: number | null
}

export type LessonDetails = {
  id: number
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
  id: number
  text: string
  answers: TestAnswer[]
}

export type TestAnswer = {
  id: number
  text: string
}

export type SubmitAnswer = {
  questionId: number
  answerId: number
}

export type SubmitTestRequest = {
  answers: SubmitAnswer[]
}

export type SubmitTestResponse = {
  passed: boolean
  scorePercent: number
  passPercent: number
  nextLessonId: number | null
  courseCompleted: boolean
}

export type CourseProgress = {
  completedLessons: number
  totalLessons: number
  progressPercent: number
  courseCompleted: boolean
}
