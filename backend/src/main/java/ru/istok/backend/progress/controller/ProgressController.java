package ru.istok.backend.progress.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.istok.backend.progress.dto.CourseProgressResponse;
import ru.istok.backend.progress.service.ProgressService;

@RestController
@RequestMapping("/api/course/progress")
@RequiredArgsConstructor
@Tag(name = "Course progress", description = "Current user course progress")
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping
    @Operation(summary = "Get current user course progress")
    public CourseProgressResponse getCourseProgress() {
        return progressService.getCourseProgress();
    }
}
