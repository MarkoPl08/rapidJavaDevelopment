package com.grade.rapidjavadevelopment.mvc.services;

import com.grade.rapidjavadevelopment.models.Role;
import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.mvc.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void saveUser(User user) {
        if (user.getRole() == null) {
            user.setRole(Role.ROLE_USER);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
