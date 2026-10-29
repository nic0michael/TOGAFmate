package com.togafmate.backend.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.togafmate.backend.controller.KnowledgeBasePageResponse;
import com.togafmate.backend.entity.KnowledgeBase;
import com.togafmate.backend.entity.KnowledgeBasePage;
import com.togafmate.backend.repository.KnowledgeBasePageRepository;
import com.togafmate.backend.repository.KnowledgeBaseRepository;

/** Provides upload and retrieval operations for database-backed Markdown pages. */
@Service
public class KnowledgeBasePageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KnowledgeBasePageService.class);
    private static final int MAX_FILE_SIZE = 1_048_576;
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final KnowledgeBasePageRepository pageRepository;

    /** Creates the service with its repositories. */
    public KnowledgeBasePageService(KnowledgeBaseRepository knowledgeBaseRepository,
            KnowledgeBasePageRepository pageRepository) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.pageRepository = pageRepository;
    }

    /** Validates and stores an uploaded Markdown page. */
    @Transactional
    public KnowledgeBasePageResponse upload(UUID knowledgeBaseId, String filename, byte[] fileBytes) {
        String safeFilename = filename == null ? "" : filename.replace('\\', '/');
        safeFilename = safeFilename.substring(safeFilename.lastIndexOf('/') + 1);
        if (!safeFilename.toLowerCase(java.util.Locale.ROOT).endsWith(".md")) {
            throw new IllegalArgumentException("Only .md files can be uploaded");
        }
        if (fileBytes == null || fileBytes.length == 0 || fileBytes.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Markdown file must be between 1 byte and 1 MB");
        }
        String title = safeFilename.substring(0, safeFilename.length() - 3).trim();
        if (title.isBlank() || title.length() > 255 || title.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Markdown filename must have a title of at most 255 characters");
        }
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(knowledgeBaseId)
                .filter(item -> Boolean.TRUE.equals(item.getIsActive()))
                .orElseThrow(() -> new IllegalArgumentException("Active Knowledge Base was not found"));
        if (pageRepository.existsByKnowledgeBaseIdAndTitleIgnoreCase(knowledgeBaseId, title)) {
            throw new IllegalArgumentException("A page with this filename already exists in the Knowledge Base");
        }
        KnowledgeBasePage page = pageRepository.save(new KnowledgeBasePage(knowledgeBase, title,
                new String(fileBytes, StandardCharsets.UTF_8)));
        LOGGER.info("Stored Markdown page {} in Knowledge Base {}", page.getId(), knowledgeBaseId);
        return toResponse(page);
    }

    /** Lists pages for an active Knowledge Base. */
    @Transactional(readOnly = true)
    public List<KnowledgeBasePageResponse> list(UUID knowledgeBaseId) {
        requireActiveKnowledgeBase(knowledgeBaseId);
        List<KnowledgeBasePageResponse> pages = pageRepository.findAllByKnowledgeBaseIdOrderByTitleAsc(knowledgeBaseId)
                .stream().map(this::toResponse).toList();
        LOGGER.info("Retrieved {} Markdown pages for Knowledge Base {}", pages.size(), knowledgeBaseId);
        return pages;
    }

    /** Returns Markdown source for a page in an active Knowledge Base. */
    @Transactional(readOnly = true)
    public String content(UUID knowledgeBaseId, UUID pageId) {
        requireActiveKnowledgeBase(knowledgeBaseId);
        String content = pageRepository.findByIdAndKnowledgeBaseId(pageId, knowledgeBaseId)
                .map(KnowledgeBasePage::getContent)
                .orElseThrow(() -> new IllegalArgumentException("Markdown page was not found"));
        LOGGER.info("Retrieved Markdown page {} for Knowledge Base {}", pageId, knowledgeBaseId);
        return content;
    }

    private KnowledgeBase requireActiveKnowledgeBase(UUID id) {
        return knowledgeBaseRepository.findById(id).filter(item -> Boolean.TRUE.equals(item.getIsActive()))
                .orElseThrow(() -> new IllegalArgumentException("Active Knowledge Base was not found"));
    }

    private KnowledgeBasePageResponse toResponse(KnowledgeBasePage page) {
        return new KnowledgeBasePageResponse(page.getId(), page.getTitle(), page.getCreatedAt());
    }
}
