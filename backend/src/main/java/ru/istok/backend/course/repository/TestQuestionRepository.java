package ru.istok.backend.course.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.istok.backend.course.entity.TestQuestion;

public interface TestQuestionRepository extends JpaRepository<TestQuestion, UUID> {

    List<TestQuestion> findByLessonIdOrderByPositionAsc(UUID lessonId);

    @Modifying
    @Query("delete from TestQuestion question where question.lesson.id = :lessonId")
    void deleteByLessonId(@Param("lessonId") UUID lessonId);
}
