package com.togafmate.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.togafmate.backend.entity.KnowledgeBasePage;

/** Provides persistence operations for Markdown pages. */
public interface KnowledgeBasePageRepository extends JpaRepository<KnowledgeBasePage, UUID> {
    /** Lists pages in a Knowledge Base in title order. */
    List<KnowledgeBasePage> findAllByKnowledgeBaseIdOrderByTitleAsc(UUID knowledgeBaseId);
    /** Checks whether a title is already used in a Knowledge Base. */
    boolean existsByKnowledgeBaseIdAndTitleIgnoreCase(UUID knowledgeBaseId, String title);
    /** Finds a page belonging to the specified Knowledge Base. */
    Optional<KnowledgeBasePage> findByIdAndKnowledgeBaseId(UUID id, UUID knowledgeBaseId);
}
