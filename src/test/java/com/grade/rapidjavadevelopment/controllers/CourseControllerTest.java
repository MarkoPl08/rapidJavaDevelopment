package com.grade.rapidjavadevelopment.controllers;

import com.grade.rapidjavadevelopment.models.Course;
import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.services.CourseService;
import com.grade.rapidjavadevelopment.services.GradeStatisticsService;
import com.grade.rapidjavadevelopment.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private UserService userService;

    @MockBean
    private GradeStatisticsService gradeStatisticsService;

    private User testUser;
    private Course testCourse;

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

        Mockito.when(userService.findByUsername("testUser"))
                .thenReturn(testUser);

        Mockito.when(courseService.getCourseById(1L))
                .thenReturn(testCourse);
        Mockito.when(courseService.getCoursesByUser(any(User.class)))
                .thenReturn(Arrays.asList(testCourse));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testListCourses() throws Exception {
        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/list"))
                .andExpect(model().attributeExists("courses"))
                .andExpect(model().attributeExists("courseAverages"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testShowCreateForm() throws Exception {
        mockMvc.perform(get("/courses/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/form"))
                .andExpect(model().attributeExists("course"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testCreateCourse() throws Exception {
        mockMvc.perform(post("/courses/create")
                        .param("courseName", "New Course")
                        .param("courseCode", "NEW101")
                        .param("credits", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"));

        Mockito.verify(courseService).saveCourse(any(Course.class), any(User.class));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testUpdateCourse() throws Exception {
        mockMvc.perform(post("/courses/edit/1")
                        .param("courseName", "Updated Course")
                        .param("courseCode", "UPD101")
                        .param("credits", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"));

        Mockito.verify(courseService).saveCourse(any(Course.class));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testDeleteCourse() throws Exception {
        mockMvc.perform(post("/courses/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"));

        Mockito.verify(courseService).deleteCourse(1L);
    }

    @Test
    @WithMockUser(username = "testUser")
    void testCreateCourseWithError() throws Exception {
        Mockito.doThrow(new RuntimeException("Test error"))
                .when(courseService).saveCourse(any(Course.class), any(User.class));

        mockMvc.perform(post("/courses/create")
                        .param("courseName", "New Course")
                        .param("courseCode", "NEW101")
                        .param("credits", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testUpdateCourseWithError() throws Exception {
        Mockito.doThrow(new RuntimeException("Test error"))
                .when(courseService).saveCourse(any(Course.class));

        mockMvc.perform(post("/courses/edit/1")
                        .param("courseName", "Updated Course")
                        .param("courseCode", "UPD101")
                        .param("credits", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testDeleteCourseWithError() throws Exception {
        Mockito.doThrow(new RuntimeException("Test error"))
                .when(courseService).deleteCourse(1L);

        mockMvc.perform(post("/courses/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testListCoursesWithError() throws Exception {
        Mockito.when(gradeStatisticsService.getCourseAverages()).thenThrow(new RuntimeException("Test error"));

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/list"));
    }


    @Test
    @WithMockUser(username = "testUser")
    void testShowEditFormWithError() throws Exception {
        Mockito.when(courseService.getCourseById(1L)).thenThrow(new RuntimeException("Test error"));

        mockMvc.perform(get("/courses/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"));

        Mockito.verify(courseService).getCourseById(1L);
    }

    @Test
    @WithMockUser(username = "testUser")
    void testDeleteCourseWithGetCourseError() throws Exception {
        Mockito.when(courseService.getCourseById(1L)).thenThrow(new RuntimeException("Test error"));

        mockMvc.perform(post("/courses/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testListCoursesWithEmptyCourses() throws Exception {
        Mockito.when(courseService.getCoursesByUser(testUser)).thenReturn(List.of());

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/list"))
                .andExpect(model().attribute("courses", List.of()));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testListCoursesWithNullStatisticsLastUpdate() throws Exception {
        Mockito.when(gradeStatisticsService.getLastUpdateTime()).thenReturn(null);

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/list"));
    }
}