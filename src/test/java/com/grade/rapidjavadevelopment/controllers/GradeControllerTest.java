package com.grade.rapidjavadevelopment.controllers;

import com.grade.rapidjavadevelopment.models.Course;
import com.grade.rapidjavadevelopment.models.Grade;
import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.services.CourseService;
import com.grade.rapidjavadevelopment.services.GradeService;
import com.grade.rapidjavadevelopment.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class GradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GradeService gradeService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private UserService userService;

    private User testUser;
    private Course testCourse;
    private Grade testGrade;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCourseName("Test Course");
        testCourse.setCourseCode("TEST101");
        testCourse.setCredits(3);
        testCourse.setStudents(new HashSet<>(Arrays.asList(testUser)));

        testGrade = new Grade();
        testGrade.setId(1L);
        testGrade.setStudent(testUser);
        testGrade.setCourse(testCourse);
        testGrade.setGrade(85.0);
        testGrade.setCreatedAt(LocalDateTime.now());
        testGrade.setUpdatedAt(LocalDateTime.now());

        Mockito.when(userService.findByUsername("testUser")).thenReturn(testUser);
        Mockito.when(courseService.getCourseById(1L)).thenReturn(testCourse);
        Mockito.when(gradeService.getGradeById(1L)).thenReturn(testGrade);
        Mockito.when(gradeService.getGradesByCourseAndStudent(any(Course.class), any(User.class)))
                .thenReturn(Arrays.asList(testGrade));
        Mockito.when(gradeService.calculateCourseGPA(any(Course.class), any(User.class)))
                .thenReturn(3.5);
    }

    @Test
    @WithMockUser(username = "testUser")
    void listGrades_shouldDisplayGradesList() throws Exception {
        mockMvc.perform(get("/courses/1/grades"))
                .andExpect(status().isOk())
                .andExpect(view().name("grades/list"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("grades"))
                .andExpect(model().attributeExists("courseGPA"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void showAddGradeForm_shouldDisplayForm() throws Exception {
        mockMvc.perform(get("/courses/1/grades/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("grades/form"))
                .andExpect(model().attributeExists("grade"))
                .andExpect(model().attributeExists("course"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void addGrade_shouldRedirectToGradesList() throws Exception {
        mockMvc.perform(post("/courses/1/grades/add")
                        .param("grade", "85.0")
                        .param("course.id", "1")
                        .param("student.id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1/grades"));

        Mockito.verify(gradeService).saveGrade(any(Grade.class));
    }

    @Test
    @WithMockUser(username = "testUser")
    void deleteGrade_whenUserOwnsGrade_shouldDeleteAndRedirect() throws Exception {
        mockMvc.perform(post("/courses/1/grades/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1/grades"));

        Mockito.verify(gradeService).deleteGrade(1L);
    }

    @Test
    @WithMockUser(username = "otherUser")
    void deleteGrade_whenUserDoesNotOwnGrade_shouldRedirectWithoutDeleting() throws Exception {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otherUser");
        Mockito.when(userService.findByUsername("otherUser")).thenReturn(otherUser);

        mockMvc.perform(post("/courses/1/grades/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1/grades"));

        Mockito.verify(gradeService, Mockito.never()).deleteGrade(1L);
    }

    @Test
    void accessGrades_withoutAuth_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/courses/1/grades"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void addGrade_withInvalidCourseId_shouldHandleError() throws Exception {
        Mockito.when(courseService.getCourseById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        mockMvc.perform(post("/courses/999/grades/add")
                        .param("grade", "85.0"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testUser")
    void listGrades_whenCourseHasNoGrades_shouldShowEmptyList() throws Exception {
        Mockito.when(gradeService.getGradesByCourseAndStudent(any(Course.class), any(User.class)))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/courses/1/grades"))
                .andExpect(status().isOk())
                .andExpect(view().name("grades/list"))
                .andExpect(model().attribute("grades", Arrays.asList()));
    }
}