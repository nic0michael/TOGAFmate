package com.togafmate.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.togafmate.backend.service.KnowledgeBaseService;
import com.togafmate.backend.entity.KnowledgeBase;
import com.togafmate.backend.validate.KnowledgeBaseValidator;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/** Exposes Knowledge Base metadata endpoints. */
@RestController
@RequestMapping("/api/knowledge-bases")
public class KnowledgeBaseController {
    private final KnowledgeBaseService knowledgeBaseService;
    private final KnowledgeBaseValidator knowledgeBaseValidator;

    /** Creates the controller with its application service. */
    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService,
            KnowledgeBaseValidator knowledgeBaseValidator) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.knowledgeBaseValidator = knowledgeBaseValidator;
    }

    /** Returns all registered Knowledge Bases. */
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public List<KnowledgeBaseResponse> getKnowledgeBases() {
        return knowledgeBaseService.findAll();
    }

    /** Validates and registers Knowledge Base metadata. */
    @PostMapping
    @Operation(summary = "Create Knowledge Base", description = "Creates Knowledge Base metadata. The API generates the UUID; do not supply an ID.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<KnowledgeBaseResponse> createKnowledgeBase(@RequestBody KnowledgeBaseRequest request) {
        KnowledgeBase knowledgeBase = new KnowledgeBase(request.title(), request.folderNameVersion(),
                request.version(), request.author(), request.emailAddress(), request.tableOfContents(),
                request.description(), request.contentType());
        List<String> errors = knowledgeBaseValidator.validate(knowledgeBase);
        if (!errors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.join("; ", errors));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(knowledgeBaseService.save(knowledgeBase));
    }

    /** Validates and updates metadata for an existing Knowledge Base. */
    @PutMapping("/{id}")
    @Operation(summary = "Update Knowledge Base", description = "Updates existing metadata. Supply the existing Knowledge Base UUID in the path.")
    @SecurityRequirement(name = "bearerAuth")
    public KnowledgeBaseResponse updateKnowledgeBase(@PathVariable UUID id,
            @RequestBody KnowledgeBaseRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Knowledge Base metadata is required");
        }
        KnowledgeBase metadata = new KnowledgeBase(request.title(), request.folderNameVersion(),
                request.version(), request.author(), request.emailAddress(), request.tableOfContents(),
                request.description(), request.contentType());
        List<String> errors = knowledgeBaseValidator.validate(metadata, id);
        if (!errors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.join("; ", errors));
        }
        try {
            return knowledgeBaseService.update(id, metadata);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        }
    }
}
