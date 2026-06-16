package ru.istok.backend.progress.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.istok.backend.progress.entity.LessonProgress;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    List<LessonProgress> findByUserIdAndLessonIdIn(Long userId, Collection<Long> lessonIds);

    long countByUserIdAndLessonCourseIdAndPassedTrue(Long userId, Long courseId);
}
