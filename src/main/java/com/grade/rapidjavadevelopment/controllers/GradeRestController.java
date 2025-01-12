package com.grade.rapidjavadevelopment.controllers;

import com.grade.rapidjavadevelopment.models.Course;
import com.grade.rapidjavadevelopment.models.Grade;
import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.services.CourseService;
import com.grade.rapidjavadevelopment.services.GradeService;
import com.grade.rapidjavadevelopment.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/grades")
public class GradeRestController {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Grade>> getGradesForCourse(@PathVariable Long courseId, Authentication authentication) {
        Course course = courseService.getCourseById(courseId);
        User currentUser = userService.findByUsername(authentication.getName());
        List<Grade> grades = gradeService.getGradesByCourseAndStudent(course, currentUser);
        return ResponseEntity.ok(grades);
    }

    @PostMapping
    public ResponseEntity<Grade> addGrade(@PathVariable Long courseId, @RequestBody Grade grade, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        Course course = courseService.getCourseById(courseId);

        grade.setStudent(currentUser);
        grade.setCourse(course);
        grade.setCreatedAt(LocalDateTime.now());
        grade.setUpdatedAt(LocalDateTime.now());

        Grade savedGrade = gradeService.saveGrade(grade);
        return ResponseEntity.ok(savedGrade);
    }

    @DeleteMapping("/{gradeId}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long courseId, @PathVariable Long gradeId, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        Grade grade = gradeService.getGradeById(gradeId);

        if (grade.getStudent().getId().equals(currentUser.getId())) {
            gradeService.deleteGrade(gradeId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/gpa")
    public ResponseEntity<Double> getCourseGPA(@PathVariable Long courseId, Authentication authentication) {
        Course course = courseService.getCourseById(courseId);
        User currentUser = userService.findByUsername(authentication.getName());
        Double gpa = gradeService.calculateCourseGPA(course, currentUser);
        return ResponseEntity.ok(gpa);
    }
}