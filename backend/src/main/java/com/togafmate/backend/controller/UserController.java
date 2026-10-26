package com.togafmate.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.togafmate.backend.service.UserService;

/** Exposes user registration, logon, and permission management endpoints. */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    /** Creates the user controller with its application service. */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Registers a new user with no access by default. */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserCredentialsRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credentials are required");
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(userService.register(request.userID(), request.password()));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    /** Verifies a user's credentials without returning the password. */
    @PostMapping("/logon")
    public UserResponse logon(@RequestBody UserCredentialsRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credentials are required");
        }
        return userService.logon(request.userID(), request.password())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    }

    /** Adds a permission to the identified user. */
    @PutMapping("/{userID}/permissions")
    @SecurityRequirement(name = "basicAuth")
    public UserResponse addPermission(@PathVariable String userID, @RequestBody PermissionRequest request) {
        validatePermissionRequest(request);
        return updatePermission(() -> userService.addPermission(userID, request.permission()));
    }

    /** Removes a permission from the identified user. */
    @DeleteMapping("/{userID}/permissions")
    @SecurityRequirement(name = "basicAuth")
    public UserResponse removePermission(@PathVariable String userID, @RequestBody PermissionRequest request) {
        validatePermissionRequest(request);
        return updatePermission(() -> userService.removePermission(userID, request.permission()));
    }

    private void validatePermissionRequest(PermissionRequest request) {
        if (request == null || request.permission() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Permission is required");
        }
    }

    private UserResponse updatePermission(java.util.function.Supplier<UserResponse> operation) {
        try {
            return operation.get();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        }
    }
}
