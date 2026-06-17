package ru.istok.backend.course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.istok.backend.course.dto.CourseResponse;
import ru.istok.backend.course.dto.LessonListItemResponse;
import ru.istok.backend.course.dto.LessonResponse;
import ru.istok.backend.course.dto.LessonSubmitRequest;
import ru.istok.backend.course.dto.LessonSubmitResponse;
import ru.istok.backend.course.service.CourseService;

@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
@Tag(name = "Курс", description = "Курс, уроки и тесты")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Получение информации о курсе")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Краткая информация о курсе"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    public CourseResponse getCourse() {
        return courseService.getCourse();
    }

    @GetMapping("/lessons")
    @Operation(summary = "Получение списка уроков с учетом прогресса пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список уроков"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    public List<LessonListItemResponse> getLessons() {
        return courseService.getLessons();
    }

    @GetMapping("/lessons/{lessonId}")
    @Operation(summary = "Получение содержимого урока и теста")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Материал урока и вопросы теста"),
            @ApiResponse(responseCode = "403", description = "Урок пока недоступен"),
            @ApiResponse(responseCode = "404", description = "Урок не найден")
    })
    public LessonResponse getLesson(
            @Parameter(description = "Идентификатор урока", required = true)
            @PathVariable UUID lessonId
    ) {
        return courseService.getLesson(lessonId);
    }

    @PostMapping("/lessons/{lessonId}/submit")
    @Operation(summary = "Отправка ответов на тест")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Результат проверки теста"),
            @ApiResponse(responseCode = "400", description = "Некорректная структура ответов"),
            @ApiResponse(responseCode = "403", description = "Урок пока недоступен"),
            @ApiResponse(responseCode = "404", description = "Урок не найден")
    })
    public LessonSubmitResponse submitLesson(
            @Parameter(description = "Идентификатор урока", required = true)
            @PathVariable UUID lessonId,
            @Valid
            @RequestBody(description = "Ответы студента на вопросы теста", required = true)
            @org.springframework.web.bind.annotation.RequestBody LessonSubmitRequest request
    ) {
        return courseService.submitLesson(lessonId, request);
    }
}
