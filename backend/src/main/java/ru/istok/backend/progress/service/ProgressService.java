package ru.istok.backend.progress.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.istok.backend.course.service.CourseService;
import ru.istok.backend.progress.dto.CourseProgressResponse;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final CourseService courseService;

    public CourseProgressResponse getCourseProgress() {
        return courseService.getProgress();
    }
}
