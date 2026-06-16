package ru.istok.backend.course.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.istok.backend.course.entity.TestAnswer;

public interface TestAnswerRepository extends JpaRepository<TestAnswer, Long> {

    List<TestAnswer> findByQuestionIdInOrderByQuestionIdAscPositionAsc(Collection<Long> questionIds);
}
