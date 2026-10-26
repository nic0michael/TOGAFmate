package com.togafmate.backend.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.togafmate.backend.entity.KnowledgeBase;
import com.togafmate.backend.repository.KnowledgeBaseRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(authorities = "ADMINISTRATOR_ACCESS")
class KnowledgeBaseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;

    @BeforeEach
    void clearKnowledgeBases() {
        knowledgeBaseRepository.deleteAll();
    }

    @Test
    void should_returnEmptyList_whenNoKnowledgeBasesExist() throws Exception {
        mockMvc.perform(get("/api/knowledge-bases"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void should_returnStoredKnowledgeBase_whenKnowledgeBaseExists() throws Exception {
        knowledgeBaseRepository.save(new KnowledgeBase("Architecture", "1.0", "Reference material",
                "Free", "architecture-v1"));

        mockMvc.perform(get("/api/knowledge-bases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Architecture"))
                .andExpect(jsonPath("$[0].version").value("1.0"))
                .andExpect(jsonPath("$[0].contentType").value("Free"));
    }

    @Test
    void should_rejectInvalidKnowledgeBase_beforeCallingService_whenMetadataIsInvalid() throws Exception {
        mockMvc.perform(post("/api/knowledge-bases")
                        .contentType("application/json")
                        .content("""
                                {"title":" ","folderNameVersion":"../outside","version":"1.0",
                                 "description":null,"contentType":"Free"}
                                """))
                .andExpect(status().isBadRequest());

        org.assertj.core.api.Assertions.assertThat(knowledgeBaseRepository.findAll()).isEmpty();
    }

    @Test
    void should_validateThenCreateKnowledgeBase_whenMetadataIsValid() throws Exception {
        mockMvc.perform(post("/api/knowledge-bases")
                        .contentType("application/json")
                        .content("""
                                {"title":"Architecture","folderNameVersion":"Architecture_Guide_v1.0",
                                 "version":"1.0","contentType":"Free"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Architecture"))
                .andExpect(jsonPath("$.folderNameVersion").value("Architecture_Guide_v1.0"));
    }
}
