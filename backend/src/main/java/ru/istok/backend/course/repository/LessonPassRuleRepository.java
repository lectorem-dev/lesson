package ru.istok.backend.course.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.istok.backend.course.entity.LessonPassRule;

public interface LessonPassRuleRepository extends JpaRepository<LessonPassRule, Long> {

    Optional<LessonPassRule> findByLessonId(Long lessonId);
}
