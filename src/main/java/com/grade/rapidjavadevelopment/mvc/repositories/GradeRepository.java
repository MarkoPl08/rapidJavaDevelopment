package com.grade.rapidjavadevelopment.mvc.repositories;

import com.grade.rapidjavadevelopment.models.Course;
import com.grade.rapidjavadevelopment.models.Grade;
import com.grade.rapidjavadevelopment.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudent(User student);
    List<Grade> findByCourse(Course course);
    List<Grade> findByCourseAndStudentOrderByCreatedAtDesc(Course course, User student);
}