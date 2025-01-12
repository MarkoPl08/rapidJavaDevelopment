package com.grade.rapidjavadevelopment.services;

import com.grade.rapidjavadevelopment.models.Course;
import com.grade.rapidjavadevelopment.models.Grade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeStatisticsServiceTest {

    @Mock
    private GradeService gradeService;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private GradeStatisticsService statisticsService;

    private Course testCourse1;
    private Course testCourse2;
    private Grade grade1;
    private Grade grade2;

    @BeforeEach
    void setUp() {
        testCourse1 = new Course();
        testCourse1.setId(1L);
        testCourse1.setCourseName("Test Course 1");

        testCourse2 = new Course();
        testCourse2.setId(2L);
        testCourse2.setCourseName("Test Course 2");

        grade1 = new Grade();
        grade1.setGrade(90.0);
        grade1.setCourse(testCourse1);

        grade2 = new Grade();
        grade2.setGrade(80.0);
        grade2.setCourse(testCourse1);
    }

    @Test
    void updateCourseStatistics_shouldCalculateAveragesForAllCourses() {
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(testCourse1, testCourse2));
        when(gradeService.getGradesByCourse(testCourse1))
                .thenReturn(Arrays.asList(grade1, grade2));
        when(gradeService.getGradesByCourse(testCourse2))
                .thenReturn(Collections.emptyList());

        statisticsService.updateCourseStatistics();

        assertThat(statisticsService.getCourseAverage(1L)).isEqualTo(85.0);
        assertThat(statisticsService.getCourseAverage(2L)).isEqualTo(0.0);
        assertThat(statisticsService.getLastUpdateTime()).isNotNull();

        verify(courseService).getAllCourses();
        verify(gradeService).getGradesByCourse(testCourse1);
        verify(gradeService).getGradesByCourse(testCourse2);
    }

    @Test
    void updateCourseStatistics_whenNoGrades_shouldSetZeroAverage() {
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(testCourse1));
        when(gradeService.getGradesByCourse(testCourse1))
                .thenReturn(Collections.emptyList());

        statisticsService.updateCourseStatistics();

        assertThat(statisticsService.getCourseAverage(1L)).isZero();
        verify(courseService).getAllCourses();
        verify(gradeService).getGradesByCourse(testCourse1);
    }

    @Test
    void updateCourseStatistics_shouldUpdateLastUpdateTime() {
        LocalDateTime beforeUpdate = LocalDateTime.now();
        when(courseService.getAllCourses()).thenReturn(Collections.emptyList());

        statisticsService.updateCourseStatistics();

        assertThat(statisticsService.getLastUpdateTime())
                .isNotNull()
                .isAfter(beforeUpdate);
    }

    @Test
    void getCourseAverage_whenCourseExists_shouldReturnAverage() {
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(testCourse1));
        when(gradeService.getGradesByCourse(testCourse1))
                .thenReturn(Arrays.asList(grade1, grade2));
        statisticsService.updateCourseStatistics();

        Double average = statisticsService.getCourseAverage(1L);

        assertThat(average).isEqualTo(85.0);
    }

    @Test
    void getCourseAverage_whenCourseDoesNotExist_shouldReturnZero() {
        Double average = statisticsService.getCourseAverage(999L);

        assertThat(average).isZero();
    }

    @Test
    void getCourseAverages_shouldReturnCopyOfMap() {
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(testCourse1));
        when(gradeService.getGradesByCourse(testCourse1))
                .thenReturn(Arrays.asList(grade1, grade2));
        statisticsService.updateCourseStatistics();

        Map<Long, Double> averages = statisticsService.getCourseAverages();
        averages.put(3L, 100.0);

        assertThat(statisticsService.getCourseAverage(3L)).isZero();
    }

    @Test
    void updateCourseStatistics_withMultipleUpdates_shouldMaintainCorrectAverages() {
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(testCourse1));

        when(gradeService.getGradesByCourse(testCourse1))
                .thenReturn(Arrays.asList(grade1));
        statisticsService.updateCourseStatistics();
        assertThat(statisticsService.getCourseAverage(1L)).isEqualTo(90.0);

        Grade grade3 = new Grade();
        grade3.setGrade(70.0);
        when(gradeService.getGradesByCourse(testCourse1))
                .thenReturn(Arrays.asList(grade1, grade3));
        statisticsService.updateCourseStatistics();

        assertThat(statisticsService.getCourseAverage(1L)).isEqualTo(80.0);
    }
}