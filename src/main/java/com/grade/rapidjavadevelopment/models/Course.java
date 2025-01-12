package com.grade.rapidjavadevelopment.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"grades"})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseName;
    private String courseCode;
    private int credits;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "user_courses",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> students = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Grade> grades = new HashSet<>();

    public void addGrade(Grade grade) {
        grades.add(grade);
        grade.setCourse(this);
    }

    public void removeGrade(Grade grade) {
        grades.remove(grade);
        grade.setCourse(null);
    }
}