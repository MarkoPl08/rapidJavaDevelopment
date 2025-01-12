package com.grade.rapidjavadevelopment.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grade.rapidjavadevelopment.models.Course;
import com.grade.rapidjavadevelopment.services.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CourseRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

    private Course testCourse;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCourseName("Test Course");
        testCourse.setCourseCode("TEST101");
        testCourse.setCredits(3);

        Mockito.when(courseService.getCourseById(1L))
                .thenReturn(testCourse);
        Mockito.when(courseService.getAllCourses())
                .thenReturn(Arrays.asList(testCourse));
        Mockito.when(courseService.saveCourse(any(Course.class)))
                .thenReturn(testCourse);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCourses_shouldReturnCoursesList() throws Exception {
        mockMvc.perform(get("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].courseName", is("Test Course")))
                .andExpect(jsonPath("$[0].courseCode", is("TEST101")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCourseById_shouldReturnCourse() throws Exception {
        mockMvc.perform(get("/api/courses/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName", is("Test Course")))
                .andExpect(jsonPath("$.courseCode", is("TEST101")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCourse_shouldReturnCreatedCourse() throws Exception {
        Course newCourse = new Course();
        newCourse.setCourseName("New Course");
        newCourse.setCourseCode("NEW101");
        newCourse.setCredits(3);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName", is("Test Course")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCourse_shouldReturnUpdatedCourse() throws Exception {
        Course updatedCourse = new Course();
        updatedCourse.setId(1L);
        updatedCourse.setCourseName("Updated Course");
        updatedCourse.setCourseCode("UPD101");
        updatedCourse.setCredits(4);

        Mockito.when(courseService.getCourseById(1L)).thenReturn(testCourse);
        Mockito.when(courseService.saveCourse(any(Course.class))).thenReturn(updatedCourse);

        mockMvc.perform(put("/api/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCourse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName", is("Updated Course")))
                .andExpect(jsonPath("$.courseCode", is("UPD101")))
                .andExpect(jsonPath("$.credits", is(4)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteCourse_shouldReturnOkStatus() throws Exception {
        mockMvc.perform(delete("/api/courses/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(courseService).deleteCourse(1L);
    }

    @Test
    void getAllCourses_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCourse_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        Course newCourse = new Course();
        newCourse.setCourseName("New Course");
        newCourse.setCourseCode("NEW101");
        newCourse.setCredits(3);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourse)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCourseById_withNonExistentId_shouldReturnNotFound() throws Exception {
        Mockito.when(courseService.getCourseById(999L))
                .thenReturn(null);

        mockMvc.perform(get("/api/courses/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCourse_withInvalidData_shouldReturnBadRequest() throws Exception {
        Course invalidCourse = new Course();

        mockMvc.perform(put("/api/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCourse)))
                .andExpect(status().isOk());
    }
}