package ru.istok.backend.course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@Tag(name = "Course", description = "Course, lessons and lesson tests")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Get course summary")
    public CourseResponse getCourse() {
        return courseService.getCourse();
    }

    @GetMapping("/lessons")
    @Operation(summary = "Get course lessons")
    public List<LessonListItemResponse> getLessons() {
        return courseService.getLessons();
    }

    @GetMapping("/lessons/{lessonId}")
    @Operation(summary = "Get lesson with test")
    public LessonResponse getLesson(@PathVariable Long lessonId) {
        return courseService.getLesson(lessonId);
    }

    @PostMapping("/lessons/{lessonId}/submit")
    @Operation(summary = "Submit lesson test answers")
    public LessonSubmitResponse submitLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonSubmitRequest request
    ) {
        return courseService.submitLesson(lessonId, request);
    }
}
