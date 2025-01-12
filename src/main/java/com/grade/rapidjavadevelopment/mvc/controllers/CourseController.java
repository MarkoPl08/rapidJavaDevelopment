package com.grade.rapidjavadevelopment.mvc.controllers;

import com.grade.rapidjavadevelopment.models.Course;
import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.mvc.services.CourseService;
import com.grade.rapidjavadevelopment.mvc.services.GradeStatisticsService;
import com.grade.rapidjavadevelopment.mvc.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private GradeStatisticsService gradeStatisticsService;

    @GetMapping
    public String listCourses(Model model, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        List<Course> courses = courseService.getCoursesByUser(currentUser); // Changed this line
        model.addAttribute("courses", courses);
        model.addAttribute("courseAverages", gradeStatisticsService.getCourseAverages());
        model.addAttribute("statisticsLastUpdate", gradeStatisticsService.getLastUpdateTime());
        return "courses/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("course", new Course());
        return "courses/form";
    }

    @PostMapping("/create")
    public String createCourse(@ModelAttribute Course course, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        courseService.saveCourse(course, currentUser);
        return "redirect:/courses";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        Course course = courseService.getCourseById(id);

        if (course.getStudents().contains(currentUser)) {
            model.addAttribute("course", course);
            return "courses/form";
        }
        return "redirect:/courses";
    }

    @PostMapping("/edit/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute Course course, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        Course existingCourse = courseService.getCourseById(id);

        if (existingCourse.getStudents().contains(currentUser)) {
            existingCourse.setCourseName(course.getCourseName());
            existingCourse.setCourseCode(course.getCourseCode());
            existingCourse.setCredits(course.getCredits());
            courseService.saveCourse(existingCourse, currentUser);
        }
        return "redirect:/courses";
    }

    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        Course course = courseService.getCourseById(id);

        if (course.getStudents().contains(currentUser)) {
            courseService.deleteCourse(id);
        }
        return "redirect:/courses";
    }
}