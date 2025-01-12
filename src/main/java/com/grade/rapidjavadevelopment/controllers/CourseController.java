package com.grade.rapidjavadevelopment.controllers;

import com.grade.rapidjavadevelopment.models.Course;
import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.services.CourseService;
import com.grade.rapidjavadevelopment.services.GradeStatisticsService;
import com.grade.rapidjavadevelopment.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private GradeStatisticsService gradeStatisticsService;

    @GetMapping
    public String listCourses(Model model, Authentication authentication) {
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            List<Course> courses = courseService.getCoursesByUser(currentUser);
            model.addAttribute("courses", courses);
            model.addAttribute("courseAverages", gradeStatisticsService.getCourseAverages());
            model.addAttribute("statisticsLastUpdate", gradeStatisticsService.getLastUpdateTime());
        } catch (Exception e) {
            logger.error("Error fetching course list or statistics: {}", e.getMessage());
            model.addAttribute("courses", List.of());
            model.addAttribute("courseAverages", List.of());
            model.addAttribute("statisticsLastUpdate", null);
        }
        return "courses/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("course", new Course());
        return "courses/form";
    }

    @PostMapping("/create")
    public String createCourse(@ModelAttribute Course course, Authentication authentication) {
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            courseService.saveCourse(course, currentUser);
            return "redirect:/courses";
        } catch (Exception e) {
            logger.error("Error creating course", e);
            return "redirect:/courses";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Course course = courseService.getCourseById(id);
            model.addAttribute("course", course);
            return "courses/form";
        } catch (Exception e) {
            logger.error("Error showing edit form", e);
            return "redirect:/courses";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute Course course, Authentication authentication) {
        try {
            Course existingCourse = courseService.getCourseById(id);

            existingCourse.setCourseName(course.getCourseName());
            existingCourse.setCourseCode(course.getCourseCode());
            existingCourse.setCredits(course.getCredits());

            courseService.saveCourse(existingCourse);

            return "redirect:/courses";
        } catch (Exception e) {
            logger.error("Error updating course", e);
            return "redirect:/courses";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id, Authentication authentication) {
        try {
            courseService.deleteCourse(id);
            return "redirect:/courses";
        } catch (Exception e) {
            logger.error("Error deleting course", e);
            return "redirect:/courses";
        }
    }
}