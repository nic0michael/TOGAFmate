package com.togafmate.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.togafmate.backend.controller.KnowledgeBaseResponse;
import com.togafmate.backend.entity.KnowledgeBase;
import com.togafmate.backend.repository.KnowledgeBaseRepository;

/** Provides application operations for Knowledge Base metadata. */
@Service
public class KnowledgeBaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KnowledgeBaseService.class);

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    /** Creates the service with its persistence repository. */
    public KnowledgeBaseService(KnowledgeBaseRepository knowledgeBaseRepository) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
    }

    /** Returns all registered Knowledge Bases. */
    public List<KnowledgeBaseResponse> findAll() {
        List<KnowledgeBaseResponse> knowledgeBases = knowledgeBaseRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        LOGGER.info("Retrieved {} Knowledge Bases", knowledgeBases.size());
        return knowledgeBases;
    }

    /** Saves a validated Knowledge Base and returns its metadata. */
    public KnowledgeBaseResponse save(KnowledgeBase knowledgeBase) {
        KnowledgeBaseResponse response = toResponse(knowledgeBaseRepository.save(knowledgeBase));
        LOGGER.info("Registered Knowledge Base {} version {}", response.title(), response.version());
        return response;
    }

    private KnowledgeBaseResponse toResponse(KnowledgeBase knowledgeBase) {
        return new KnowledgeBaseResponse(knowledgeBase.getId(), knowledgeBase.getTitle(),
                knowledgeBase.getVersion(), knowledgeBase.getDescription(), knowledgeBase.getContentType(),
                knowledgeBase.getFolderNameVersion(), knowledgeBase.getAuthor(), knowledgeBase.getEmailAddress(),
                knowledgeBase.getTableOfContents());
    }
}
