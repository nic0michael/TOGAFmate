package com.togafmate.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.togafmate.backend.service.KnowledgeBaseService;
import com.togafmate.backend.entity.KnowledgeBase;
import com.togafmate.backend.validate.KnowledgeBaseValidator;
import org.springframework.web.server.ResponseStatusException;

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
    public List<KnowledgeBaseResponse> getKnowledgeBases() {
        return knowledgeBaseService.findAll();
    }

    /** Validates and registers Knowledge Base metadata. */
    @PostMapping
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
}
