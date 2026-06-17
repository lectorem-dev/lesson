package ru.istok.backend.course.repository;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.istok.backend.course.entity.Course;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findByTitle(String title);
}
