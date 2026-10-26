# TOGAFmate Backend

Spring Boot REST API using Java 21, Spring Data JPA, PostgreSQL, and Gradle Kotlin DSL. The API currently exposes a health endpoint and a Knowledge Base metadata listing endpoint. Swagger documents the API.

## Run

Configure a PostgreSQL database, then run `gradlew.bat bootRun` on Windows or `./gradlew bootRun` on macOS/Linux. Defaults target `jdbc:postgresql://localhost:5432/togafmate` with username `postgres` and an empty password. Override these with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. The default JPA schema mode is `update`; set `JPA_DDL_AUTO` to change it.

## Test

Run `gradlew.bat test` on Windows or `./gradlew test` on macOS/Linux. JaCoCo reports are written to `build/reports/jacoco/test/`.

## API testing

When the application is running, use Swagger UI at <http://localhost:8080/swagger-ui/index.html> or the OpenAPI document at <http://localhost:8080/v3/api-docs>.

The endpoints are `GET /api/health`, which returns `{"status":"UP"}`, `GET /api/knowledge-bases`, which returns registered Knowledge Base metadata, and `POST /api/knowledge-bases`, which validates and registers metadata before persistence.

The POST endpoint requires `title`, `folderNameVersion`, `version`, and `contentType`. Content type must be `Free`, `Open-source`, or `Copyrighted`. `author`, `emailAddress`, `tableOfContents`, and `description` are optional; a supplied email address must be valid. The folder identifier must be a safe, unique folder name.

The Knowledge Base metadata table is managed by Spring Data JPA. Tests use an in-memory H2 database, so PostgreSQL does not need to be running to execute `gradlew.bat test`.

## CORS

The API allows the Angular development origin `http://localhost:4200` by default. Set `APP_CORS_ALLOWED_ORIGINS` to a comma-separated list of exact frontend origins when running in another environment.
