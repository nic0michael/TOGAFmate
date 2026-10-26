package com.togafmate.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.togafmate.backend.entity.User;
import com.togafmate.backend.entity.UserPermission;
import com.togafmate.backend.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void should_registerAndLogonWithoutReturningPassword_whenCredentialsAreValid() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userID\":\"reader1\",\"password\":\"secret\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.permissions[0]").value("NO_ACCESS"))
                .andExpect(jsonPath("$.password").doesNotExist());

        assertThat(userRepository.findById("reader1").orElseThrow().getPassword()).isNotEqualTo("secret");

        mockMvc.perform(post("/api/users/logon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userID\":\"reader1\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userID").value("reader1"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void should_rejectLogon_whenPasswordIsIncorrect() throws Exception {
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userID\":\"reader1\",\"password\":\"secret\"}"));

        mockMvc.perform(post("/api/users/logon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userID\":\"reader1\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_requireAdministrator_whenChangingPermissions() throws Exception {
        userRepository.save(new User("reader1", "encoded-password"));

        mockMvc.perform(put("/api/users/reader1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permission\":\"USER_ACCESS\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ADMINISTRATOR_ACCESS")
    void should_addPermission_whenAdministratorRequestsChange() throws Exception {
        userRepository.save(new User("reader1", "encoded-password"));

        mockMvc.perform(put("/api/users/reader1/permissions")
                        .with(user("admin").authorities(() -> "ADMINISTRATOR_ACCESS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permission\":\"USER_ACCESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions[0]").value("USER_ACCESS"));
    }

    @Test
    void should_removePermissionAndRestoreNoAccess_whenLastPermissionIsRemoved() throws Exception {
        userRepository.save(new User("reader1", "encoded-password", List.of(UserPermission.USER_ACCESS)));

        mockMvc.perform(delete("/api/users/reader1/permissions")
                        .with(user("admin").authorities(() -> "ADMINISTRATOR_ACCESS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permission\":\"USER_ACCESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions[0]").value("NO_ACCESS"));
    }

    @Test
    void should_denyKnowledgeBaseAccess_whenUserHasNoAccessPermission() throws Exception {
        userRepository.save(new User("reader1", passwordEncoder.encode("secret")));

        mockMvc.perform(get("/api/knowledge-bases").with(httpBasic("reader1", "secret")))
                .andExpect(status().isForbidden());
    }
}
