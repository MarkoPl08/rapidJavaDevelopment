package com.grade.rapidjavadevelopment.controllers;

import com.grade.rapidjavadevelopment.models.Course;
import com.grade.rapidjavadevelopment.models.Grade;
import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.services.CourseService;
import com.grade.rapidjavadevelopment.services.GradeService;
import com.grade.rapidjavadevelopment.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/courses/{courseId}/grades")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listGrades(@PathVariable Long courseId, Model model, Authentication authentication) {
        Course course = courseService.getCourseById(courseId);
        User currentUser = userService.findByUsername(authentication.getName());
        model.addAttribute("course", course);
        model.addAttribute("grades", gradeService.getGradesByCourseAndStudent(course, currentUser));
        model.addAttribute("courseGPA", gradeService.calculateCourseGPA(course, currentUser));
        return "grades/list";
    }

    @GetMapping("/add")
    public String showAddGradeForm(@PathVariable Long courseId, Model model) {
        model.addAttribute("grade", new Grade());
        model.addAttribute("course", courseService.getCourseById(courseId));
        return "grades/form";
    }

    @PostMapping("/add")
    public String addGrade(@PathVariable Long courseId, @ModelAttribute Grade grade, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        Course course = courseService.getCourseById(courseId);

        grade.setStudent(currentUser);
        grade.setCourse(course);
        grade.setCreatedAt(LocalDateTime.now());
        grade.setUpdatedAt(LocalDateTime.now());

        gradeService.saveGrade(grade);
        return "redirect:/courses/" + courseId + "/grades";
    }

    @PostMapping("/delete/{gradeId}")
    public String deleteGrade(@PathVariable Long courseId, @PathVariable Long gradeId, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        Grade grade = gradeService.getGradeById(gradeId);

        if (grade.getStudent().getId().equals(currentUser.getId())) {
            gradeService.deleteGrade(gradeId);
        }

        return "redirect:/courses/" + courseId + "/grades";
    }
}