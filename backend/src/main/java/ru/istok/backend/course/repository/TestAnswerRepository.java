package ru.istok.backend.course.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.istok.backend.course.entity.TestAnswer;

public interface TestAnswerRepository extends JpaRepository<TestAnswer, UUID> {

    List<TestAnswer> findByQuestionIdInOrderByQuestionIdAscPositionAsc(Collection<UUID> questionIds);
}
