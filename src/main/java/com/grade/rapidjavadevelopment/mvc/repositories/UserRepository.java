package com.grade.rapidjavadevelopment.mvc.repositories;

import com.grade.rapidjavadevelopment.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
