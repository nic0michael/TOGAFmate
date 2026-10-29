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
import com.togafmate.backend.repository.KnowledgeBasePageRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(authorities = "ADMINISTRATOR_USER_ACCESS")
class KnowledgeBaseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;
    @Autowired
    private KnowledgeBasePageRepository pageRepository;

    @Test
    void should_uploadAndRetrieveMarkdownPage_whenAdministratorSelectsActiveKnowledgeBase() throws Exception {
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.save(new KnowledgeBase("Architecture", "1.0",
                "Reference material", "Free", "architecture-v1"));
        MockMultipartFile file = new MockMultipartFile("file", "overview.md", "text/markdown",
                "# Overview\nArchitecture content".getBytes());

        mockMvc.perform(multipart("/api/knowledge-bases/{id}/pages", knowledgeBase.getId()).file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("overview"));
        mockMvc.perform(get("/api/knowledge-bases/{id}/pages", knowledgeBase.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("overview"));
    }

    @Test
    void should_returnMarkdownPageContent_whenReaderRequestsPage() throws Exception {
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.save(new KnowledgeBase("Architecture", "1.0",
                "Reference material", "Free", "architecture-v1"));
        MockMultipartFile file = new MockMultipartFile("file", "overview.md", "text/markdown",
                "# Overview".getBytes());
        String response = mockMvc.perform(multipart("/api/knowledge-bases/{id}/pages", knowledgeBase.getId()).file(file))
                .andReturn().getResponse().getContentAsString();
        String pageId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(get("/api/knowledge-bases/{id}/pages", knowledgeBase.getId())
                        .with(user("reader").authorities(() -> "USER_ACCESS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("overview"));
        mockMvc.perform(get("/api/knowledge-bases/{id}/pages/{pageId}", knowledgeBase.getId(), pageId)
                        .with(user("reader").authorities(() -> "USER_ACCESS")))
                .andExpect(status().isOk())
                .andExpect(content().string("# Overview"));
    }

    @Test
    void should_forbidPageUpload_whenRequesterIsReader() throws Exception {
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.save(new KnowledgeBase("Architecture", "1.0",
                "Reference material", "Free", "architecture-v1"));
        MockMultipartFile file = new MockMultipartFile("file", "overview.md", "text/markdown",
                "# Overview".getBytes());
        mockMvc.perform(multipart("/api/knowledge-bases/{id}/pages", knowledgeBase.getId()).file(file)
                        .with(user("reader").authorities(() -> "USER_ACCESS")))
                .andExpect(status().isForbidden());
    }

    @BeforeEach
    void clearKnowledgeBases() {
        pageRepository.deleteAll();
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
                .andExpect(jsonPath("$[0].contentType").value("Free"))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    void should_notListInactiveKnowledgeBases_whenListingKnowledgeBases() throws Exception {
        knowledgeBaseRepository.save(new KnowledgeBase("Active Guide", "Active_Guide_v1", "1.0", null, null,
                null, null, "Free", true));
        knowledgeBaseRepository.save(new KnowledgeBase("Inactive Guide", "Inactive_Guide_v1", "1.0", null,
                null, null, null, "Free", false));

        mockMvc.perform(get("/api/knowledge-bases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title").value(org.hamcrest.Matchers.contains("Active Guide")))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    void should_listActiveAndInactiveKnowledgeBases_whenAdministratorRequestsManagementList() throws Exception {
        knowledgeBaseRepository.save(new KnowledgeBase("Active Guide", "Active_Guide_v1", "1.0", null, null,
                null, null, "Free", true));
        knowledgeBaseRepository.save(new KnowledgeBase("Inactive Guide", "Inactive_Guide_v1", "1.0", null,
                null, null, null, "Free", false));

        mockMvc.perform(get("/api/knowledge-bases/management"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title").value(org.hamcrest.Matchers.containsInAnyOrder(
                        "Active Guide", "Inactive Guide")))
                .andExpect(jsonPath("$[?(@.title == 'Inactive Guide')].isActive").value(false));
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
                                 "version":"1.0","contentType":"Free","isActive":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Architecture"))
                .andExpect(jsonPath("$.folderNameVersion").value("Architecture_Guide_v1.0"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void should_rejectKnowledgeBase_whenIsActiveIsMissing() throws Exception {
        mockMvc.perform(post("/api/knowledge-bases")
                        .contentType("application/json")
                        .content("""
                                {"title":"Architecture","folderNameVersion":"Architecture_Guide_v1.0",
                                 "version":"1.0","contentType":"Free"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_updateKnowledgeBaseMetadata_whenAdministratorRequestsUpdate() throws Exception {
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.save(new KnowledgeBase("Architecture", "1.0",
                "Old description", "Free", "architecture-v1"));

        mockMvc.perform(put("/api/knowledge-bases/" + knowledgeBase.getId())
                        .contentType("application/json")
                        .content("""
                                {"title":"Updated Architecture","folderNameVersion":"architecture-v2",
                                 "version":"2.0","description":"Updated description","contentType":"Open-source",
                                 "isActive":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(knowledgeBase.getId().toString()))
                .andExpect(jsonPath("$.title").value("Updated Architecture"))
                .andExpect(jsonPath("$.folderNameVersion").value("architecture-v2"))
                .andExpect(jsonPath("$.version").value("2.0"))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void should_forbidKnowledgeBaseUpdate_whenRequesterIsNotAdministrator() throws Exception {
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.save(new KnowledgeBase("Architecture", "1.0",
                "Old description", "Free", "architecture-v1"));

        mockMvc.perform(put("/api/knowledge-bases/" + knowledgeBase.getId())
                        .with(user("reader").authorities(() -> "USER_ACCESS"))
                        .contentType("application/json")
                        .content("""
                                {"title":"Updated Architecture","folderNameVersion":"architecture-v2",
                                 "version":"2.0","contentType":"Open-source","isActive":true}
                                """))
                .andExpect(status().isForbidden());
    }
}
