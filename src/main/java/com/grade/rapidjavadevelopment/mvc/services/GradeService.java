package com.grade.rapidjavadevelopment.mvc.services;

import com.grade.rapidjavadevelopment.models.Course;
import com.grade.rapidjavadevelopment.models.Grade;
import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.mvc.repositories.GradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    public List<Grade> getAllGrades() {
        return gradeRepository.findAll();
    }

    public List<Grade> getGradesByStudent(User student) {
        return gradeRepository.findByStudent(student);
    }

    public Grade getGradeById(Long id) {
        return gradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grade not found"));
    }

    public Grade saveGrade(Grade grade) {
        if (grade.getCreatedAt() == null) {
            grade.setCreatedAt(LocalDateTime.now());
        }
        grade.setUpdatedAt(LocalDateTime.now());

        Course course = grade.getCourse();
        course.addGrade(grade);

        return gradeRepository.save(grade);
    }

    public Grade addGrade(Long studentId, Long courseId, Double gradeValue) {
        User student = userService.getUserById(studentId);
        Course course = courseService.getCourseById(courseId);

        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setGrade(gradeValue);
        grade.setCreatedAt(LocalDateTime.now());
        grade.setUpdatedAt(LocalDateTime.now());

        course.addGrade(grade);

        return gradeRepository.save(grade);
    }

    public void deleteGrade(Long id) {
        Grade grade = getGradeById(id);
        Course course = grade.getCourse();

        course.removeGrade(grade);
        gradeRepository.deleteById(id);
    }

    public double calculateGPA(User student) {
        List<Grade> grades = gradeRepository.findByStudent(student);
        if (grades.isEmpty()) {
            return 0.0;
        }

        double totalWeightedGrade = 0;
        int totalCredits = 0;

        for (Grade grade : grades) {
            int credits = grade.getCourse().getCredits();
            totalWeightedGrade += grade.getGrade() * credits;
            totalCredits += credits;
        }

        return totalCredits > 0 ? totalWeightedGrade / totalCredits : 0.0;
    }

    public List<Grade> getGradesByCourseAndStudent(Course course, User student) {
        return gradeRepository.findByCourseAndStudentOrderByCreatedAtDesc(course, student);
    }

    public Double calculateCourseGPA(Course course, User student) {
        List<Grade> grades = getGradesByCourseAndStudent(course, student);
        if (grades.isEmpty()) return 0.0;

        return grades.stream()
                .mapToDouble(Grade::getGrade)
                .average()
                .orElse(0.0);
    }

    public List<Grade> getGradesByCourse(Course course) {
        return gradeRepository.findByCourse(course);
    }
}