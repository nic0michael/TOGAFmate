package com.togafmate.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.togafmate.backend.entity.KnowledgeBase;

/** Provides persistence operations for Knowledge Base metadata. */
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, UUID> {
    /** Checks whether a folder identifier is already registered. */
    boolean existsByFolderNameVersion(String folderNameVersion);

    /** Checks whether another Knowledge Base uses the supplied folder identifier. */
    boolean existsByFolderNameVersionAndIdNot(String folderNameVersion, UUID id);

    /** Finds Knowledge Bases whose title or description contains the supplied text. */
    List<KnowledgeBase> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String titleText, String descriptionText);
}
