package com.grade.rapidjavadevelopment.mvc.repositories;

import com.grade.rapidjavadevelopment.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
}
