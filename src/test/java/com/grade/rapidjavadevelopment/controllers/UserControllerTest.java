package com.grade.rapidjavadevelopment.controllers;

import com.grade.rapidjavadevelopment.models.Role;
import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.ROLE_USER);

        Mockito.when(userService.saveUser(any(User.class))).thenReturn(testUser);
    }

    @Test
    void showLoginPage_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void showRegistrationForm_shouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void registerUser_whenSuccessful_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "newUser")
                        .param("password", "password123")
                        .param("email", "new@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        Mockito.verify(userService).saveUser(any(User.class));
    }

    @Test
    void registerUser_whenServiceThrowsException_shouldRedirectToRegister() throws Exception {
        Mockito.when(userService.saveUser(any(User.class)))
                .thenThrow(new RuntimeException("Registration failed"));

        mockMvc.perform(post("/register")
                        .param("username", "newUser")
                        .param("password", "password123")
                        .param("email", "new@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void registerUser_shouldSetRoleToUser() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "newUser")
                .param("password", "password123")
                .param("email", "new@example.com"));

        Mockito.verify(userService).saveUser(Mockito.argThat(user ->
                user.getRole() == Role.ROLE_USER
        ));
    }

    @Test
    void registerUser_withExistingUsername_shouldRedirectToRegister() throws Exception {
        Mockito.when(userService.saveUser(any(User.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/register")
                        .param("username", "existingUser")
                        .param("password", "password123")
                        .param("email", "existing@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void registerUser_withMissingRequiredFields_shouldRedirectToRegister() throws Exception {
        Mockito.when(userService.saveUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Missing required fields"));

        mockMvc.perform(post("/register")
                        .param("username", "")
                        .param("password", "")
                        .param("email", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void registerUser_shouldNotAllowAdminRole() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "newUser")
                .param("password", "password123")
                .param("email", "new@example.com")
                .param("role", Role.ROLE_ADMIN.name()));

        Mockito.verify(userService).saveUser(Mockito.argThat(user ->
                user.getRole() == Role.ROLE_USER
        ));
    }
}