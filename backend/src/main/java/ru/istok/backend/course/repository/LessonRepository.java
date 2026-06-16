package ru.istok.backend.course.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.istok.backend.course.entity.Course;
import ru.istok.backend.course.entity.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByCourseOrderByPositionAsc(Course course);

    Optional<Lesson> findByCourseAndPosition(Course course, Integer position);

    Optional<Lesson> findByCourseIdAndPosition(Long courseId, Integer position);
}
