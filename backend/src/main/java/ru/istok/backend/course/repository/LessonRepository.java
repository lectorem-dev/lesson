package ru.istok.backend.course.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.istok.backend.course.entity.Course;
import ru.istok.backend.course.entity.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByCourseOrderByPositionAsc(Course course);

    Optional<Lesson> findByCourseAndPosition(Course course, Integer position);

}
