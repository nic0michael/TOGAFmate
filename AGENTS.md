# AGENTS.md

## 1. Purpose and General Rules

Write readable, maintainable, reliable code. Prefer the simplest solution that meets the requirements (KISS). Avoid over-engineering, clever tricks, duplication, dead code, and speculative generality. Follow SOLID where appropriate and keep methods focused.

### Project Specification and Decision Rules

- Treat the project README and other explicitly designated specification documents as the source of truth for requirements.
- Implement only the task explicitly assigned by the user.
- Do not invent or change business rules, architecture, technology choices, interfaces, or database designs without approval.
- If requirements are unclear, contradictory, incomplete, or require an architectural decision, stop and ask the user before proceeding.
- Do not perform unrelated refactoring or introduce additional features.
- Before making changes, inspect the relevant existing code and tests to understand the current implementation.
- Follow the project's established architecture and conventions. Do not replace existing technology choices with defaults from this file.
- At completion, report the files changed, tests executed and their actual results, and any outstanding issues.

### Security Requirements

- Validate input at appropriate application boundaries, especially REST API endpoints.
- Enforce authentication and authorisation in the backend; never rely on frontend controls alone for security.
- Never hardcode, expose, or commit passwords, tokens, secrets, or sensitive configuration.
- Do not log secrets, credentials, or sensitive personal or business data.
- Follow the security requirements in the project specification. If a security requirement is unclear, ask the user before making a design decision.

## 2. Java Development and Testing

### Java Coding Standard

- Use meaningful names: classes `PascalCase`, methods and variables `camelCase`, constants `UPPER_SNAKE_CASE`, packages lowercase.
- Use generics, try-with-resources, enhanced `for` loops, and `StringBuilder` where appropriate.
- Use K&R brace style.
- Document all public classes and methods with Javadoc; do not add Javadoc to private members. Avoid explanatory comments; make the code self-documenting.
- Prefer specific exceptions over error codes. Never swallow exceptions.
- Log errors with appropriate context before throwing exceptions.
- **Successful-execution logging:** Write an appropriate INFO log after successful application-level method execution, especially in service and controller methods that return values. Identify the successful operation and include relevant, non-sensitive context, such as a business identifier or result count, when useful.
- Log success only after the operation has completed successfully. Do not log success before the operation finishes or when it fails.
- Do not log private methods, getters, or setters. Avoid redundant logging at multiple layers for the same operation.
- Never log passwords, authentication tokens, secrets, or sensitive personal or business data.
- Use parameterised logging rather than string concatenation when supported by the logging framework.
- Avoid long methods/classes, long parameter lists, duplicate code, and switch statements where polymorphism is suitable.

### Java Unit Tests and Coverage

- Use JUnit 5 and AssertJ. Structure tests as Arrange / Act / Assert.
- Name tests `should_[expected_outcome]_when_[condition]`.
- Follow Red–Green–Refactor: write one failing test, implement the minimum to pass, then refactor only while tests remain green.
- Run tests after every change. Keep tests fast, independent, repeatable, self-validating, and timely.
- Use Mockito for unit tests where dependencies need to be mocked, and use JaCoCo to measure coverage.
- Aim for 100% applicable line and branch coverage where practical. Add positive, negative, and exception-path tests. Do not weaken or remove tests merely to improve coverage, and do not add meaningless tests solely to increase coverage.
- Test external failure cases, including invalid inputs, empty responses, HTTP errors, and exceptions.
- Do not leave tests commented out or ignored.

### Mocking Policy Dependency

The original policies refer to `chameleon-tdd-testing-skill.md`, but it was not supplied. Use Mockito as specified above unless the project provides an additional, explicit Chameleon policy.

## 3. Angular Development and Testing

### Angular Coding Standard

- Use TypeScript with strict typing; avoid `any` unless there is a justified reason.
- Follow Angular conventions for components, services, dependency injection, routing, and lifecycle hooks.
- Keep components focused; move reusable business logic and API access into services.
- Use reactive forms and RxJS patterns where appropriate. Clean up subscriptions and resources to prevent leaks.
- Use consistent `PascalCase` for types/classes and `camelCase` for properties and methods. Keep templates accessible and avoid complex business logic in templates.

### Angular Tests

- Unit-test components, services, pipes, validators, and important branches using the project's configured Angular testing framework.
- Mock HTTP and external dependencies. Test success, error, empty, and invalid-input cases.
- Run the project's test and coverage commands; report the actual results. Follow the project's configured coverage thresholds.

## 4. Test-Driven Development Rules

For new behaviour, use this cycle:

1. Red: Write one test and confirm it fails for the expected reason.
2. Green: Write the smallest implementation that passes.
3. Refactor: Improve design only after tests pass; keep them green.

Do not write production code before a failing test exists. Do not write multiple new tests at once or add code "just in case". Do not refactor unrelated code during a task.

The earlier policy requested a commit after each cycle, but repository rules prohibit AI Git commands and commits; a human reviewer commits after review.

## 5. Technology Defaults

Use these defaults only when the project has not specified otherwise:

| Concern | Default |
|---|---|
| Language | Java |
| Database | Microsoft SQL Server |
| Build tool | Gradle with Kotlin DSL |

Do not replace established project choices with these defaults.

## 6. Repository and Completion Rules

- Work on the existing feature branch. Do not create sub-branches.
- Do not run Git commands or commit changes. A human reviewer commits after review.
- Do not claim a test, build, coverage report, or scan passed unless it was run and the result was observed.
- Do not report unexecuted tests as passed or imply that coverage targets were met without evidence.
- At completion, summarise:
  - Changes made and files affected.
  - Tests and checks executed, with actual results.
  - Coverage results, when available.
  - Outstanding issues, limitations, or decisions requiring user approval.
- If a task cannot be completed safely or correctly because of missing requirements, dependencies, permissions, or environment problems, stop and report the blocker rather than guessing.
