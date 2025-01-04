package com.grade.rapidjavadevelopment.mvc.controllers;

import com.grade.rapidjavadevelopment.models.Role;
import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.mvc.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            user.setRole(Role.ROLE_USER);

            userService.saveUser(user);

            redirectAttributes.addFlashAttribute("successMessage", "Registration successful!");
            return "redirect:/login";
        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed. Please try again.");
            return "redirect:/register";
        }
    }
}
