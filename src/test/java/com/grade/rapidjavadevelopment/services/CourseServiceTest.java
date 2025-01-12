package com.grade.rapidjavadevelopment.services;

import com.grade.rapidjavadevelopment.models.Course;
import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.repositories.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    private Course testCourse;
    private User testUser;

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
        testCourse.setStudents(new HashSet<>());
    }

    @Test
    void getAllCourses_shouldReturnAllCourses() {
        when(courseRepository.findAll()).thenReturn(Arrays.asList(testCourse));

        List<Course> result = courseService.getAllCourses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourseName()).isEqualTo("Test Course");
        verify(courseRepository).findAll();
    }

    @Test
    void getCourseById_whenCourseExists_shouldReturnCourse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        Course result = courseService.getCourseById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getCourseName()).isEqualTo("Test Course");
        verify(courseRepository).findById(1L);
    }

    @Test
    void getCourseById_whenCourseDoesNotExist_shouldThrowException() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Course not found");
        verify(courseRepository).findById(999L);
    }

    @Test
    void saveCourse_shouldSaveAndReturnCourse() {
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        Course result = courseService.saveCourse(testCourse);

        assertThat(result).isNotNull();
        assertThat(result.getCourseName()).isEqualTo("Test Course");
        verify(courseRepository).save(testCourse);
    }

    @Test
    void saveCourse_withUser_whenUserNotInCourse_shouldAddUserAndSave() {
        Course courseToSave = new Course();
        courseToSave.setCourseName("New Course");
        courseToSave.setStudents(new HashSet<>());

        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Course result = courseService.saveCourse(courseToSave, testUser);

        assertThat(result.getStudents()).contains(testUser);
        verify(courseRepository).save(courseToSave);
    }

    @Test
    void saveCourse_withUser_whenUserAlreadyInCourse_shouldNotAddUserAgain() {
        testCourse.getStudents().add(testUser);
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        Course result = courseService.saveCourse(testCourse, testUser);

        assertThat(result.getStudents()).hasSize(1);
        assertThat(result.getStudents()).contains(testUser);
        verify(courseRepository).save(testCourse);
    }

    @Test
    void deleteCourse_shouldCallRepository() {
        courseService.deleteCourse(1L);

        verify(courseRepository).deleteById(1L);
    }

    @Test
    void getCoursesByUser_shouldReturnUsersCourses() {
        when(courseRepository.findByStudentsContaining(testUser))
                .thenReturn(Arrays.asList(testCourse));

        List<Course> result = courseService.getCoursesByUser(testUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourseName()).isEqualTo("Test Course");
        verify(courseRepository).findByStudentsContaining(testUser);
    }

    @Test
    void getCoursesByUser_whenNoCoursesFound_shouldReturnEmptyList() {
        when(courseRepository.findByStudentsContaining(testUser))
                .thenReturn(Arrays.asList());

        List<Course> result = courseService.getCoursesByUser(testUser);

        assertThat(result).isEmpty();
        verify(courseRepository).findByStudentsContaining(testUser);
    }
}