package ru.istok.backend.course.importcontent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.istok.backend.course.entity.Course;
import ru.istok.backend.course.entity.Lesson;
import ru.istok.backend.course.entity.LessonPassRule;
import ru.istok.backend.course.entity.TestAnswer;
import ru.istok.backend.course.entity.TestQuestion;
import ru.istok.backend.course.repository.CourseRepository;
import ru.istok.backend.course.repository.LessonPassRuleRepository;
import ru.istok.backend.course.repository.LessonRepository;
import ru.istok.backend.course.repository.TestAnswerRepository;
import ru.istok.backend.course.repository.TestQuestionRepository;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseContentImporter implements ApplicationRunner {

    private static final String COURSE_TITLE = "Основной курс";
    private static final String COURSE_DESCRIPTION = "Единый курс MVP платформы";

    // TODO: replace hardcoded lesson manifest with configurable course manifest
    private static final List<LessonManifestItem> MANIFEST = List.of(
            new LessonManifestItem(1, "lesson-1.md", "test-1.json"),
            new LessonManifestItem(2, "lesson-2.md", "test-2.json"),
            new LessonManifestItem(3, "lesson-3.md", "test-3.json")
    );

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final TestAnswerRepository testAnswerRepository;
    private final LessonPassRuleRepository lessonPassRuleRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.content.root:../content}")
    private String contentRoot;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            importContent();
        } catch (ContentImportException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ContentImportException("Failed to import course content: " + exception.getMessage(), exception);
        }
    }

    private void importContent() {
        Path root = Path.of(contentRoot).toAbsolutePath().normalize();
        Path lessonsDir = root.resolve("lessons");
        Path testsDir = root.resolve("tests");

        ensureDirectory(lessonsDir, "lessons");
        ensureDirectory(testsDir, "tests");
        validateExactFiles(lessonsDir, MANIFEST.stream().map(LessonManifestItem::lessonFile).collect(Collectors.toSet()));
        validateExactFiles(testsDir, MANIFEST.stream().map(LessonManifestItem::testFile).collect(Collectors.toSet()));
        validateManifestNumbers();

        Course course = courseRepository.findByTitle(COURSE_TITLE).orElseGet(Course::new);
        course.setTitle(COURSE_TITLE);
        course.setDescription(COURSE_DESCRIPTION);
        course = courseRepository.save(course);

        for (LessonManifestItem item : MANIFEST) {
            String markdown = readMarkdown(lessonsDir.resolve(item.lessonFile()));
            TestFileDto test = readTest(testsDir.resolve(item.testFile()));
            validateTest(test, item.testFile());
            upsertLesson(course, item.position(), markdown, test);
        }

        log.info("Imported course content from {}", root);
    }

    private void ensureDirectory(Path directory, String name) {
        if (!Files.isDirectory(directory)) {
            throw new ContentImportException("Content directory '%s' is missing: %s".formatted(name, directory));
        }
    }

    private void validateExactFiles(Path directory, Set<String> expectedFiles) {
        Set<String> actualFiles;
        try (var files = Files.list(directory)) {
            actualFiles = files
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toSet());
        } catch (IOException exception) {
            throw new ContentImportException("Failed to list content directory: " + directory, exception);
        }

        Set<String> missingFiles = expectedFiles.stream()
                .filter(file -> !actualFiles.contains(file))
                .collect(Collectors.toSet());
        if (!missingFiles.isEmpty()) {
            throw new ContentImportException("Missing expected content files in %s: %s".formatted(directory, missingFiles));
        }

        Set<String> extraFiles = actualFiles.stream()
                .filter(file -> !expectedFiles.contains(file))
                .collect(Collectors.toSet());
        if (!extraFiles.isEmpty()) {
            throw new ContentImportException("Unexpected content files in %s: %s".formatted(directory, extraFiles));
        }
    }

    private void validateManifestNumbers() {
        for (LessonManifestItem item : MANIFEST) {
            String expectedLessonFile = "lesson-%d.md".formatted(item.position());
            String expectedTestFile = "test-%d.json".formatted(item.position());
            if (!expectedLessonFile.equals(item.lessonFile()) || !expectedTestFile.equals(item.testFile())) {
                throw new ContentImportException(
                        "Lesson and test numbers do not match manifest position %d".formatted(item.position())
                );
            }
        }
    }

    private String readMarkdown(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new ContentImportException("Failed to read markdown file: " + path, exception);
        }
    }

    private TestFileDto readTest(Path path) {
        try {
            return objectMapper.readValue(path.toFile(), TestFileDto.class);
        } catch (Exception exception) {
            throw new ContentImportException("Invalid test JSON file '%s': %s".formatted(path, exception.getMessage()), exception);
        }
    }

    private void validateTest(TestFileDto test, String fileName) {
        if (test.getPassPercent() == null || test.getPassPercent() < 1 || test.getPassPercent() > 100) {
            throw new ContentImportException("Test '%s' must contain passPercent from 1 to 100".formatted(fileName));
        }
        if (test.getQuestions() == null || test.getQuestions().isEmpty()) {
            throw new ContentImportException("Test '%s' must contain at least one question".formatted(fileName));
        }

        for (int questionIndex = 0; questionIndex < test.getQuestions().size(); questionIndex++) {
            TestQuestionFileDto question = test.getQuestions().get(questionIndex);
            if (!StringUtils.hasText(question.getText())) {
                throw new ContentImportException("Question %d in '%s' must contain text".formatted(questionIndex + 1, fileName));
            }
            if (question.getAnswers() == null || question.getAnswers().size() < 2) {
                throw new ContentImportException("Question %d in '%s' must contain at least two answers".formatted(questionIndex + 1, fileName));
            }

            long correctCount = question.getAnswers().stream()
                    .filter(answer -> Boolean.TRUE.equals(answer.getCorrect()))
                    .count();
            long incorrectCount = question.getAnswers().stream()
                    .filter(answer -> Boolean.FALSE.equals(answer.getCorrect()))
                    .count();
            boolean hasInvalidAnswer = question.getAnswers().stream()
                    .anyMatch(answer -> !StringUtils.hasText(answer.getText()) || answer.getCorrect() == null);

            if (hasInvalidAnswer) {
                throw new ContentImportException("Question %d in '%s' contains invalid answer".formatted(questionIndex + 1, fileName));
            }
            if (correctCount != 1 || incorrectCount < 1) {
                throw new ContentImportException("Question %d in '%s' must contain exactly one correct answer and at least one incorrect answer".formatted(questionIndex + 1, fileName));
            }
        }
    }

    private void upsertLesson(Course course, int position, String markdown, TestFileDto test) {
        Lesson lesson = lessonRepository.findByCourseAndPosition(course, position).orElseGet(Lesson::new);
        lesson.setCourse(course);
        lesson.setPosition(position);
        lesson.setTitle("Урок " + position);
        lesson.setMarkdownContent(markdown);
        lesson = lessonRepository.saveAndFlush(lesson);

        LessonPassRule rule = lessonPassRuleRepository.findByLessonId(lesson.getId()).orElseGet(LessonPassRule::new);
        rule.setLesson(lesson);
        rule.setPassPercent(test.getPassPercent());
        lessonPassRuleRepository.save(rule);

        testQuestionRepository.deleteByLessonId(lesson.getId());
        testQuestionRepository.flush();

        for (int questionIndex = 0; questionIndex < test.getQuestions().size(); questionIndex++) {
            TestQuestionFileDto questionDto = test.getQuestions().get(questionIndex);
            TestQuestion question = new TestQuestion();
            question.setLesson(lesson);
            question.setPosition(questionIndex + 1);
            question.setText(questionDto.getText());
            question = testQuestionRepository.save(question);

            for (int answerIndex = 0; answerIndex < questionDto.getAnswers().size(); answerIndex++) {
                TestAnswerFileDto answerDto = questionDto.getAnswers().get(answerIndex);
                TestAnswer answer = new TestAnswer();
                answer.setQuestion(question);
                answer.setPosition(answerIndex + 1);
                answer.setText(answerDto.getText());
                answer.setCorrect(answerDto.getCorrect());
                testAnswerRepository.save(answer);
            }
        }
    }
}
