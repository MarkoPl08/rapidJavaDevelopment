package com.grade.rapidjavadevelopment.controllers;

import com.grade.rapidjavadevelopment.models.User;
import com.grade.rapidjavadevelopment.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");

        Mockito.when(userService.getUserById(1L)).thenReturn(testUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsers() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowEditUserForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/users/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-form"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser() throws Exception {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updatedUser");
        updatedUser.setEmail("updated@example.com");

        Mockito.when(userService.getUserById(1L)).thenReturn(updatedUser);
        Mockito.when(userService.saveUser(Mockito.any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/admin/users/edit/1")
                        .param("username", "updatedUser")
                        .param("email", "updated@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        Mockito.verify(userService).getUserById(1L);
        Mockito.verify(userService).saveUser(Mockito.any(User.class));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/admin/users/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        Mockito.verify(userService).deleteUser(1L);
    }
}
