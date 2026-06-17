package ru.istok.backend.progress.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.istok.backend.progress.entity.LessonProgress;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {

    Optional<LessonProgress> findByUserIdAndLessonId(UUID userId, UUID lessonId);

    List<LessonProgress> findByUserIdAndLessonIdIn(UUID userId, Collection<UUID> lessonIds);

    long countByUserIdAndLessonCourseIdAndPassedTrue(UUID userId, UUID courseId);
}
