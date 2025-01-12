package com.grade.rapidjavadevelopment.repositories;

import com.grade.rapidjavadevelopment.models.Role;
import com.grade.rapidjavadevelopment.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);
}