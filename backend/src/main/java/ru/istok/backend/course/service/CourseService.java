package ru.istok.backend.course.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.istok.backend.common.exception.CourseNotFoundException;
import ru.istok.backend.common.exception.LessonLockedException;
import ru.istok.backend.common.exception.LessonNotFoundException;
import ru.istok.backend.common.exception.SubmitValidationException;
import ru.istok.backend.common.exception.UserNotFoundException;
import ru.istok.backend.course.dto.CourseResponse;
import ru.istok.backend.course.dto.LessonListItemResponse;
import ru.istok.backend.course.dto.LessonResponse;
import ru.istok.backend.course.dto.LessonStatus;
import ru.istok.backend.course.dto.LessonSubmitAnswerRequest;
import ru.istok.backend.course.dto.LessonSubmitRequest;
import ru.istok.backend.course.dto.LessonSubmitResponse;
import ru.istok.backend.course.dto.LessonTestResponse;
import ru.istok.backend.course.dto.TestAnswerResponse;
import ru.istok.backend.course.dto.TestQuestionResponse;
import ru.istok.backend.course.entity.Course;
import ru.istok.backend.course.entity.Lesson;
import ru.istok.backend.course.entity.LessonPassRule;
import ru.istok.backend.course.entity.TestAnswer;
import ru.istok.backend.course.entity.TestQuestion;
import ru.istok.backend.course.mapper.CourseMapper;
import ru.istok.backend.course.repository.CourseRepository;
import ru.istok.backend.course.repository.LessonPassRuleRepository;
import ru.istok.backend.course.repository.LessonRepository;
import ru.istok.backend.course.repository.TestAnswerRepository;
import ru.istok.backend.course.repository.TestQuestionRepository;
import ru.istok.backend.progress.dto.CourseProgressResponse;
import ru.istok.backend.progress.entity.LessonProgress;
import ru.istok.backend.progress.repository.LessonProgressRepository;
import ru.istok.backend.security.JwtUser;
import ru.istok.backend.user.entity.User;
import ru.istok.backend.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CourseService {

    private static final String COURSE_TITLE = "Основной курс";

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final TestAnswerRepository testAnswerRepository;
    private final LessonPassRuleRepository lessonPassRuleRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;

    @Transactional(readOnly = true)
    public CourseResponse getCourse() {
        User user = getCurrentUser();
        Course course = getMainCourse();
        List<Lesson> lessons = getCourseLessons(course);
        int completedLessons = countCompletedLessons(user.getId(), course.getId());

        return courseMapper.toCourseResponse(course, completedLessons, lessons.size());
    }

    @Transactional(readOnly = true)
    public List<LessonListItemResponse> getLessons() {
        User user = getCurrentUser();
        Course course = getMainCourse();
        List<Lesson> lessons = getCourseLessons(course);
        Map<UUID, LessonProgress> progressByLessonId = getProgressByLessonId(user.getId(), lessons);

        List<LessonListItemResponse> response = new ArrayList<>();
        for (Lesson lesson : lessons) {
            LessonStatus status = getLessonStatus(lesson, progressByLessonId);
            LessonProgress progress = progressByLessonId.get(lesson.getId());
            response.add(new LessonListItemResponse(
                    lesson.getId(),
                    lesson.getPosition(),
                    lesson.getTitle(),
                    status,
                    status == LessonStatus.LOCKED,
                    progress == null ? null : progress.getBestScorePercent()
            ));
        }

        return response;
    }

    @Transactional(readOnly = true)
    public LessonResponse getLesson(UUID lessonId) {
        User user = getCurrentUser();
        Course course = getMainCourse();
        List<Lesson> lessons = getCourseLessons(course);
        Lesson lesson = findLessonInCourse(lessonId, lessons);
        Map<UUID, LessonProgress> progressByLessonId = getProgressByLessonId(user.getId(), lessons);

        if (getLessonStatus(lesson, progressByLessonId) == LessonStatus.LOCKED) {
            throw new LessonLockedException();
        }

        LessonPassRule rule = getPassRule(lesson.getId());
        List<TestQuestion> questions = testQuestionRepository.findByLessonIdOrderByPositionAsc(lesson.getId());
        Map<UUID, List<TestAnswer>> answersByQuestionId = getAnswersByQuestionId(questions);

        List<TestQuestionResponse> questionResponses = questions.stream()
                .map(question -> toQuestionResponse(question, answersByQuestionId.getOrDefault(question.getId(), List.of())))
                .toList();

        return new LessonResponse(
                lesson.getId(),
                lesson.getPosition(),
                lesson.getTitle(),
                lesson.getMarkdownContent(),
                new LessonTestResponse(rule.getPassPercent(), questionResponses)
        );
    }

    @Transactional
    public LessonSubmitResponse submitLesson(UUID lessonId, LessonSubmitRequest request) {
        User user = getCurrentUser();
        Course course = getMainCourse();
        List<Lesson> lessons = getCourseLessons(course);
        Lesson lesson = findLessonInCourse(lessonId, lessons);
        Map<UUID, LessonProgress> progressByLessonId = getProgressByLessonId(user.getId(), lessons);

        if (getLessonStatus(lesson, progressByLessonId) == LessonStatus.LOCKED) {
            throw new LessonLockedException();
        }

        LessonPassRule rule = getPassRule(lesson.getId());
        List<TestQuestion> questions = testQuestionRepository.findByLessonIdOrderByPositionAsc(lesson.getId());
        Map<UUID, List<TestAnswer>> answersByQuestionId = getAnswersByQuestionId(questions);
        Map<UUID, TestQuestion> questionById = questions.stream()
                .collect(Collectors.toMap(TestQuestion::getId, Function.identity()));

        validateSubmitRequest(request, questionById.keySet(), answersByQuestionId);

        Map<UUID, UUID> selectedAnswerByQuestionId = request.getAnswers().stream()
                .collect(Collectors.toMap(
                        LessonSubmitAnswerRequest::getQuestionId,
                        LessonSubmitAnswerRequest::getAnswerId
                ));

        long correctAnswers = questions.stream()
                .filter(question -> isCorrectAnswer(question.getId(), selectedAnswerByQuestionId.get(question.getId()), answersByQuestionId))
                .count();
        int scorePercent = (int) Math.round(correctAnswers * 100.0 / questions.size());
        boolean passed = scorePercent >= rule.getPassPercent();

        if (passed) {
            savePassedProgress(user, lesson, scorePercent);
        }

        UUID nextLessonId = passed ? getNextLessonId(lesson, lessons) : null;
        boolean courseCompleted = passed && countCompletedLessons(user.getId(), course.getId()) == lessons.size();

        return new LessonSubmitResponse(
                passed,
                scorePercent,
                rule.getPassPercent(),
                nextLessonId,
                courseCompleted
        );
    }

    @Transactional(readOnly = true)
    public CourseProgressResponse getProgress() {
        User user = getCurrentUser();
        Course course = getMainCourse();
        List<Lesson> lessons = getCourseLessons(course);
        int completedLessons = countCompletedLessons(user.getId(), course.getId());

        return courseMapper.toProgressResponse(completedLessons, lessons.size());
    }

    private TestQuestionResponse toQuestionResponse(TestQuestion question, List<TestAnswer> answers) {
        List<TestAnswerResponse> answerResponses = answers.stream()
                .map(answer -> new TestAnswerResponse(answer.getId(), answer.getText()))
                .collect(Collectors.toCollection(ArrayList::new));
        // Порядок ответов перемешивается на каждой загрузке урока, чтобы нельзя было запомнить позицию правильного варианта.
        Collections.shuffle(answerResponses);

        return new TestQuestionResponse(question.getId(), question.getText(), answerResponses);
    }

    private void validateSubmitRequest(
            LessonSubmitRequest request,
            Set<UUID> expectedQuestionIds,
            Map<UUID, List<TestAnswer>> answersByQuestionId
    ) {
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new SubmitValidationException("Нужно передать ответы на все вопросы");
        }

        // Клиент должен прислать ровно по одному ответу на каждый вопрос именно этого урока.
        Set<UUID> requestQuestionIds = new HashSet<>();
        for (LessonSubmitAnswerRequest answer : request.getAnswers()) {
            if (answer.getQuestionId() == null || answer.getAnswerId() == null) {
                throw new SubmitValidationException("Для каждого ответа нужны questionId и answerId");
            }
            if (!requestQuestionIds.add(answer.getQuestionId())) {
                throw new SubmitValidationException("В запросе найден повторяющийся questionId: " + answer.getQuestionId());
            }
        }

        Set<UUID> missingQuestionIds = expectedQuestionIds.stream()
                .filter(questionId -> !requestQuestionIds.contains(questionId))
                .collect(Collectors.toSet());
        if (!missingQuestionIds.isEmpty()) {
            throw new SubmitValidationException("Нет ответов для вопросов: " + missingQuestionIds);
        }

        Set<UUID> extraQuestionIds = requestQuestionIds.stream()
                .filter(questionId -> !expectedQuestionIds.contains(questionId))
                .collect(Collectors.toSet());
        if (!extraQuestionIds.isEmpty()) {
            throw new SubmitValidationException("В запросе есть лишние вопросы: " + extraQuestionIds);
        }

        for (LessonSubmitAnswerRequest answer : request.getAnswers()) {
            boolean belongsToQuestion = answersByQuestionId.getOrDefault(answer.getQuestionId(), List.of())
                    .stream()
                    .anyMatch(testAnswer -> testAnswer.getId().equals(answer.getAnswerId()));
            if (!belongsToQuestion) {
                throw new SubmitValidationException(
                        "Ответ %s не относится к вопросу %s".formatted(answer.getAnswerId(), answer.getQuestionId())
                );
            }
        }
    }

    private boolean isCorrectAnswer(UUID questionId, UUID answerId, Map<UUID, List<TestAnswer>> answersByQuestionId) {
        return answersByQuestionId.getOrDefault(questionId, List.of())
                .stream()
                .anyMatch(answer -> answer.getId().equals(answerId) && Boolean.TRUE.equals(answer.getCorrect()));
    }

    private void savePassedProgress(User user, Lesson lesson, int scorePercent) {
        LessonProgress progress = lessonProgressRepository.findByUserIdAndLessonId(user.getId(), lesson.getId())
                .orElseGet(LessonProgress::new);

        progress.setUser(user);
        progress.setLesson(lesson);
        progress.setPassed(true);
        // completedAt фиксируем по первой успешной попытке, а лучший процент можем повышать последующими попытками.
        progress.setCompletedAt(progress.getCompletedAt() == null ? LocalDateTime.now() : progress.getCompletedAt());
        progress.setBestScorePercent(progress.getBestScorePercent() == null
                ? scorePercent
                : Math.max(progress.getBestScorePercent(), scorePercent));

        lessonProgressRepository.save(progress);
    }

    private UUID getNextLessonId(Lesson lesson, List<Lesson> lessons) {
        return lessons.stream()
                .filter(candidate -> candidate.getPosition().equals(lesson.getPosition() + 1))
                .map(Lesson::getId)
                .findFirst()
                .orElse(null);
    }

    private LessonStatus getLessonStatus(Lesson lesson, Map<UUID, LessonProgress> progressByLessonId) {
        LessonProgress currentProgress = progressByLessonId.get(lesson.getId());
        if (currentProgress != null && Boolean.TRUE.equals(currentProgress.getPassed())) {
            return LessonStatus.PASSED;
        }

        if (lesson.getPosition() == 1) {
            return LessonStatus.AVAILABLE;
        }

        // Урок открывается только после прохождения предыдущего урока по порядковому номеру.
        boolean previousPassed = progressByLessonId.values()
                .stream()
                .anyMatch(progress -> progress.getLesson().getPosition().equals(lesson.getPosition() - 1)
                        && Boolean.TRUE.equals(progress.getPassed()));

        return previousPassed ? LessonStatus.AVAILABLE : LessonStatus.LOCKED;
    }

    private Map<UUID, LessonProgress> getProgressByLessonId(UUID userId, List<Lesson> lessons) {
        List<UUID> lessonIds = lessons.stream().map(Lesson::getId).toList();
        if (lessonIds.isEmpty()) {
            return Map.of();
        }

        return lessonProgressRepository.findByUserIdAndLessonIdIn(userId, lessonIds)
                .stream()
                .collect(Collectors.toMap(progress -> progress.getLesson().getId(), Function.identity()));
    }

    private Map<UUID, List<TestAnswer>> getAnswersByQuestionId(List<TestQuestion> questions) {
        List<UUID> questionIds = questions.stream().map(TestQuestion::getId).toList();
        if (questionIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, List<TestAnswer>> answersByQuestionId = new HashMap<>();
        for (TestAnswer answer : testAnswerRepository.findByQuestionIdInOrderByQuestionIdAscPositionAsc(questionIds)) {
            answersByQuestionId.computeIfAbsent(answer.getQuestion().getId(), ignored -> new ArrayList<>()).add(answer);
        }

        return answersByQuestionId;
    }

    private Lesson findLessonInCourse(UUID lessonId, List<Lesson> lessons) {
        return lessons.stream()
                .filter(lesson -> lesson.getId().equals(lessonId))
                .findFirst()
                .orElseThrow(LessonNotFoundException::new);
    }

    private Course getMainCourse() {
        return courseRepository.findByTitle(COURSE_TITLE)
                .orElseThrow(CourseNotFoundException::new);
    }

    private List<Lesson> getCourseLessons(Course course) {
        return lessonRepository.findByCourseOrderByPositionAsc(course);
    }

    private LessonPassRule getPassRule(UUID lessonId) {
        return lessonPassRuleRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new IllegalStateException("Для урока %s не найдено правило прохождения".formatted(lessonId)));
    }

    private int countCompletedLessons(UUID userId, UUID courseId) {
        return Math.toIntExact(lessonProgressRepository.countByUserIdAndLessonCourseIdAndPassedTrue(userId, courseId));
    }

    private User getCurrentUser() {
        Object principal = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (!(principal instanceof JwtUser jwtUser)) {
            throw new UserNotFoundException();
        }

        return userRepository.findById(jwtUser.userId())
                .orElseThrow(UserNotFoundException::new);
    }
}
