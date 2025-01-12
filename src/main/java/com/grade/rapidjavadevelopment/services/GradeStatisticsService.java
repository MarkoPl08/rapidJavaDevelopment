package com.grade.rapidjavadevelopment.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class GradeStatisticsService {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private CourseService courseService;

    private Map<Long, Double> courseAverages = new HashMap<>();
    @Getter
    private LocalDateTime lastUpdateTime;

    @Scheduled(fixedRate = 30000)
    public void updateCourseStatistics() {
        System.out.println("Updating course statistics at: " + LocalDateTime.now());

        courseService.getAllCourses().forEach(course -> {
            double avgGrade = gradeService.getGradesByCourse(course).stream()
                    .mapToDouble(grade -> grade.getGrade())
                    .average()
                    .orElse(0.0);

            courseAverages.put(course.getId(), avgGrade);
        });

        lastUpdateTime = LocalDateTime.now();
        System.out.println("Statistics update completed. Processed " + courseAverages.size() + " courses.");
    }

    public Double getCourseAverage(Long courseId) {
        return courseAverages.getOrDefault(courseId, 0.0);
    }

    public Map<Long, Double> getCourseAverages() {
        return new HashMap<>(courseAverages);
    }
}