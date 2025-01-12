package com.grade.rapidjavadevelopment.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
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
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class GradeRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        Mockito.when(gradeService.saveGrade(any(Grade.class))).thenReturn(testGrade);
    }

    @Test
    @WithMockUser(username = "testUser")
    void getGradesForCourse_shouldReturnGradesList() throws Exception {
        mockMvc.perform(get("/api/courses/1/grades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].grade", is(85.0)));
    }

    @Test
    @WithMockUser(username = "testUser")
    void addGrade_shouldReturnSavedGrade() throws Exception {
        Grade newGrade = new Grade();
        newGrade.setGrade(90.0);

        mockMvc.perform(post("/api/courses/1/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGrade)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade", is(85.0)));

        Mockito.verify(gradeService).saveGrade(any(Grade.class));
    }

    @Test
    @WithMockUser(username = "testUser")
    void deleteGrade_whenUserOwnsGrade_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/courses/1/grades/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(gradeService).deleteGrade(1L);
    }

    @Test
    @WithMockUser(username = "otherUser")
    void deleteGrade_whenUserDoesNotOwnGrade_shouldReturnForbidden() throws Exception {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otherUser");
        Mockito.when(userService.findByUsername("otherUser")).thenReturn(otherUser);

        mockMvc.perform(delete("/api/courses/1/grades/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        Mockito.verify(gradeService, Mockito.never()).deleteGrade(1L);
    }

    @Test
    @WithMockUser(username = "testUser")
    void getCourseGPA_shouldReturnGPA() throws Exception {
        mockMvc.perform(get("/api/courses/1/grades/gpa")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(3.5)));
    }

    @Test
    void accessGrades_withoutAuth_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/courses/1/grades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser")
    void getGradesForCourse_withInvalidCourseId_shouldReturnNotFound() throws Exception {
        Mockito.when(courseService.getCourseById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        mockMvc.perform(get("/api/courses/999/grades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testUser")
    void addGrade_withInvalidCourseId_shouldReturnNotFound() throws Exception {
        Grade newGrade = new Grade();
        newGrade.setGrade(90.0);

        Mockito.when(courseService.getCourseById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        mockMvc.perform(post("/api/courses/999/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGrade)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testUser")
    void getGradesForCourse_whenNoGrades_shouldReturnEmptyList() throws Exception {
        Mockito.when(gradeService.getGradesByCourseAndStudent(any(Course.class), any(User.class)))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/courses/1/grades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}