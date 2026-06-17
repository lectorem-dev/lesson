package ru.istok.backend.course.repository;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.istok.backend.course.entity.LessonPassRule;

public interface LessonPassRuleRepository extends JpaRepository<LessonPassRule, UUID> {

    Optional<LessonPassRule> findByLessonId(UUID lessonId);
}
