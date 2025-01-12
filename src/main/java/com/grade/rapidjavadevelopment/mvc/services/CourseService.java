package com.grade.rapidjavadevelopment.mvc.services;

import com.grade.rapidjavadevelopment.models.Course;
import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.mvc.repositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    public Course saveCourse(Course course, User user) {
        if (!course.getStudents().contains(user)) {
            course.getStudents().add(user);
        }
        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    public List<Course> getCoursesByUser(User user) {
        return courseRepository.findByStudentsContaining(user);
    }
}