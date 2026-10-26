package com.togafmate.backend.controller;

/** Represents metadata submitted when registering a Knowledge Base. */
public record KnowledgeBaseRequest(String title, String folderNameVersion, String version, String author,
        String emailAddress, String tableOfContents, String description, String contentType) {
}
