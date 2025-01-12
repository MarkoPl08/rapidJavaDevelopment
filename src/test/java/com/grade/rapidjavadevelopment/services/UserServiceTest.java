package com.grade.rapidjavadevelopment.services;

import com.grade.rapidjavadevelopment.models.Role;
import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.ROLE_USER);
    }

    @Test
    void saveUser_withNewUser_shouldEncodePasswordAndSave() {
        User newUser = new User();
        newUser.setUsername("newUser");
        newUser.setPassword("password123");
        newUser.setEmail("new@example.com");
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        userService.saveUser(newUser);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void saveUser_withNoRole_shouldSetDefaultRole() {
        User newUser = new User();
        newUser.setUsername("newUser");
        newUser.setPassword("password123");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        userService.saveUser(newUser);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    void saveUser_withExistingRole_shouldNotChangeRole() {
        testUser.setRole(Role.ROLE_ADMIN);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User savedUser = userService.saveUser(testUser);

        assertThat(savedUser.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void findByUsername_whenUserExists_shouldReturnUser() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        User foundUser = userService.findByUsername("testUser");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("testUser");
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    void findByUsername_whenUserDoesNotExist_shouldThrowException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("nonexistent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User foundUser = userService.getUserById(1L);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        List<User> users = userService.getAllUsers();

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUsername()).isEqualTo("testUser");
        verify(userRepository).findAll();
    }

    @Test
    void getAllStudents_shouldReturnOnlyStudentUsers() {
        User adminUser = new User();
        adminUser.setRole(Role.ROLE_ADMIN);
        when(userRepository.findByRole(Role.ROLE_USER)).thenReturn(Arrays.asList(testUser));

        List<User> students = userService.getAllStudents();

        assertThat(students).hasSize(1);
        assertThat(students.get(0).getRole()).isEqualTo(Role.ROLE_USER);
        verify(userRepository).findByRole(Role.ROLE_USER);
    }

    @Test
    void deleteUser_shouldCallRepository() {
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void saveUser_withNullPassword_shouldNotEncodePassword() {
        testUser.setPassword(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.saveUser(testUser);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository).save(testUser);
    }

    @Test
    void saveUser_withEmptyPassword_shouldEncodePassword() {
        testUser.setPassword("");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.saveUser(testUser);

        verify(passwordEncoder).encode("");
        verify(userRepository).save(testUser);
    }
}