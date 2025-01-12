package com.grade.rapidjavadevelopment.mvc.repositories;

import com.grade.rapidjavadevelopment.models.Course;
import com.grade.rapidjavadevelopment.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByStudentsContaining(User user);
}
