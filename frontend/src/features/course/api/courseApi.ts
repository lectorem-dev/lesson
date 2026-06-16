import { httpClient } from '../../../shared/api/httpClient'
import type {
  CourseProgress,
  CourseSummary,
  LessonDetails,
  LessonListItem,
  SubmitTestRequest,
  SubmitTestResponse,
} from '../model/courseTypes'

export async function getCourse() {
  const response = await httpClient.get<CourseSummary>('/api/course')
  return response.data
}

export async function getLessons() {
  const response = await httpClient.get<LessonListItem[]>('/api/course/lessons')
  return response.data
}

export async function getLessonById(lessonId: number) {
  const response = await httpClient.get<LessonDetails>(`/api/course/lessons/${lessonId}`)
  return response.data
}

export async function submitLessonTest(lessonId: number, request: SubmitTestRequest) {
  const response = await httpClient.post<SubmitTestResponse>(
    `/api/course/lessons/${lessonId}/submit`,
    request,
  )
  return response.data
}

export async function getProgress() {
  const response = await httpClient.get<CourseProgress>('/api/course/progress')
  return response.data
}
