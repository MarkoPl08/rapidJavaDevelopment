package com.grade.rapidjavadevelopment.services;

import com.grade.rapidjavadevelopment.models.Course;
import com.grade.rapidjavadevelopment.models.Grade;
import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.repositories.GradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private UserService userService;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private GradeService gradeService;

    @Captor
    private ArgumentCaptor<Grade> gradeCaptor;

    private User testUser;
    private Course testCourse;
    private Grade testGrade;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCourseName("Test Course");
        testCourse.setCredits(3);

        testGrade = new Grade();
        testGrade.setId(1L);
        testGrade.setGrade(85.0);
        testGrade.setStudent(testUser);
        testGrade.setCourse(testCourse);
        testGrade.setCreatedAt(LocalDateTime.now());
        testGrade.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getAllGrades_shouldReturnAllGrades() {
        when(gradeRepository.findAll()).thenReturn(Arrays.asList(testGrade));

        List<Grade> result = gradeService.getAllGrades();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGrade()).isEqualTo(85.0);
        verify(gradeRepository).findAll();
    }

    @Test
    void getGradesByStudent_shouldReturnStudentGrades() {
        when(gradeRepository.findByStudent(testUser)).thenReturn(Arrays.asList(testGrade));

        List<Grade> result = gradeService.getGradesByStudent(testUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudent()).isEqualTo(testUser);
        verify(gradeRepository).findByStudent(testUser);
    }

    @Test
    void getGradeById_whenGradeExists_shouldReturnGrade() {
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(testGrade));

        Grade result = gradeService.getGradeById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getGrade()).isEqualTo(85.0);
        verify(gradeRepository).findById(1L);
    }

    @Test
    void getGradeById_whenGradeDoesNotExist_shouldThrowException() {
        when(gradeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.getGradeById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Grade not found");
    }

    @Test
    void saveGrade_withNewGrade_shouldSetTimestamps() {
        Grade newGrade = new Grade();
        newGrade.setGrade(90.0);
        newGrade.setCourse(testCourse);
        when(gradeRepository.save(any(Grade.class))).thenReturn(newGrade);

        gradeService.saveGrade(newGrade);

        verify(gradeRepository).save(gradeCaptor.capture());
        Grade savedGrade = gradeCaptor.getValue();
        assertThat(savedGrade.getCreatedAt()).isNotNull();
        assertThat(savedGrade.getUpdatedAt()).isNotNull();
    }

    @Test
    void saveGrade_withExistingGrade_shouldUpdateTimestamp() {
        LocalDateTime originalCreatedAt = testGrade.getCreatedAt();
        when(gradeRepository.save(any(Grade.class))).thenReturn(testGrade);

        gradeService.saveGrade(testGrade);

        verify(gradeRepository).save(gradeCaptor.capture());
        Grade savedGrade = gradeCaptor.getValue();
        assertThat(savedGrade.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(savedGrade.getUpdatedAt()).isAfter(originalCreatedAt);
    }

    @Test
    void addGrade_shouldCreateNewGrade() {
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(courseService.getCourseById(1L)).thenReturn(testCourse);
        when(gradeRepository.save(any(Grade.class))).thenReturn(testGrade);

        Grade result = gradeService.addGrade(1L, 1L, 85.0);

        assertThat(result).isNotNull();
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    void addGrade_whenUserNotFound_shouldThrowException() {
        when(userService.getUserById(1L)).thenThrow(new RuntimeException("User not found"));

        assertThatThrownBy(() -> gradeService.addGrade(1L, 1L, 85.0))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void deleteGrade_shouldRemoveGradeAndUpdateCourse() {
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(testGrade));

        gradeService.deleteGrade(1L);

        verify(gradeRepository).deleteById(1L);
    }

    @Test
    void deleteGrade_whenGradeNotFound_shouldThrowException() {
        when(gradeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.deleteGrade(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Grade not found");
    }

    @Test
    void calculateGPA_withMultipleGrades_shouldCalculateCorrectly() {
        Course course1 = new Course();
        course1.setCredits(3);
        Course course2 = new Course();
        course2.setCredits(4);

        Grade grade1 = new Grade();
        grade1.setGrade(90.0);
        grade1.setCourse(course1);

        Grade grade2 = new Grade();
        grade2.setGrade(80.0);
        grade2.setCourse(course2);

        when(gradeRepository.findByStudent(testUser)).thenReturn(Arrays.asList(grade1, grade2));

        double gpa = gradeService.calculateGPA(testUser);

        assertThat(gpa).isEqualTo(84.28571428571429);
    }

    @Test
    void calculateGPA_withNoGrades_shouldReturnZero() {
        when(gradeRepository.findByStudent(testUser)).thenReturn(Collections.emptyList());

        double gpa = gradeService.calculateGPA(testUser);

        assertThat(gpa).isZero();
    }

    @Test
    void calculateGPA_withZeroCredits_shouldReturnZero() {
        Grade gradeWithZeroCredits = new Grade();
        gradeWithZeroCredits.setGrade(90.0);
        Course zeroCreditsCourse = new Course();
        zeroCreditsCourse.setCredits(0);
        gradeWithZeroCredits.setCourse(zeroCreditsCourse);

        when(gradeRepository.findByStudent(testUser)).thenReturn(Arrays.asList(gradeWithZeroCredits));

        double gpa = gradeService.calculateGPA(testUser);

        assertThat(gpa).isZero();
    }

    @Test
    void getGradesByCourseAndStudent_shouldReturnOrderedGrades() {
        when(gradeRepository.findByCourseAndStudentOrderByCreatedAtDesc(testCourse, testUser))
                .thenReturn(Arrays.asList(testGrade));

        List<Grade> result = gradeService.getGradesByCourseAndStudent(testCourse, testUser);

        assertThat(result).hasSize(1);
        verify(gradeRepository).findByCourseAndStudentOrderByCreatedAtDesc(testCourse, testUser);
    }

    @Test
    void calculateCourseGPA_withMultipleGrades_shouldCalculateAverage() {
        Grade grade1 = new Grade();
        grade1.setGrade(90.0);
        Grade grade2 = new Grade();
        grade2.setGrade(80.0);

        when(gradeRepository.findByCourseAndStudentOrderByCreatedAtDesc(testCourse, testUser))
                .thenReturn(Arrays.asList(grade1, grade2));

        double gpa = gradeService.calculateCourseGPA(testCourse, testUser);

        assertThat(gpa).isEqualTo(85.0);
    }

    @Test
    void calculateCourseGPA_withNoGrades_shouldReturnZero() {
        when(gradeRepository.findByCourseAndStudentOrderByCreatedAtDesc(testCourse, testUser))
                .thenReturn(Collections.emptyList());

        double gpa = gradeService.calculateCourseGPA(testCourse, testUser);

        assertThat(gpa).isZero();
    }

    @Test
    void getGradesByCourse_shouldReturnCourseGrades() {
        when(gradeRepository.findByCourse(testCourse)).thenReturn(Arrays.asList(testGrade));

        List<Grade> result = gradeService.getGradesByCourse(testCourse);

        assertThat(result).hasSize(1);
        verify(gradeRepository).findByCourse(testCourse);
    }
}
