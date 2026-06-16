package ru.istok.backend.course.mapper;

import org.springframework.stereotype.Component;
import ru.istok.backend.course.dto.CourseResponse;
import ru.istok.backend.course.entity.Course;
import ru.istok.backend.progress.dto.CourseProgressResponse;

@Component
public class CourseMapper {

    public CourseResponse toCourseResponse(Course course, int completedLessons, int totalLessons) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                calculateProgressPercent(completedLessons, totalLessons),
                completedLessons,
                totalLessons
        );
    }

    public CourseProgressResponse toProgressResponse(int completedLessons, int totalLessons) {
        return new CourseProgressResponse(
                completedLessons,
                totalLessons,
                calculateProgressPercent(completedLessons, totalLessons),
                totalLessons > 0 && completedLessons == totalLessons
        );
    }

    public int calculateProgressPercent(int completedLessons, int totalLessons) {
        if (totalLessons == 0) {
            return 0;
        }

        return (int) Math.round(completedLessons * 100.0 / totalLessons);
    }
}
