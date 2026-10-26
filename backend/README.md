# TOGAFmate Backend

Spring Boot REST API using Java 21, Spring Data JPA, Spring Security, PostgreSQL, and Gradle Kotlin DSL. Swagger documents the API. User passwords are stored as BCrypt hashes.

## Run

Configure a PostgreSQL database, then run `gradlew.bat bootRun` on Windows or `./gradlew bootRun` on macOS/Linux. Defaults target `jdbc:postgresql://localhost:5432/togafmate` with username `postgres` and an empty password. Override these with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. The default JPA schema mode is `update`; set `JPA_DDL_AUTO` to change it.

## Test

Run `gradlew.bat test` on Windows or `./gradlew test` on macOS/Linux. JaCoCo reports are written to `build/reports/jacoco/test/`.

## API testing

When the application is running, use Swagger UI at <http://localhost:8080/swagger-ui/index.html> or the OpenAPI document at <http://localhost:8080/v3/api-docs>.

The endpoints include `GET /api/health`, `GET /api/knowledge-bases`, `POST /api/knowledge-bases`, `POST /api/users/register`, and `POST /api/users/logon`. Registration creates accounts with `NO_ACCESS`; logon verifies credentials and returns the user's permissions without the password.

The logon lookup uses a named JPQL parameter for `userID`; passwords are BCrypt-encoded and compared outside the query. Raw passwords are never queried or returned.

The POST endpoint requires `title`, `folderNameVersion`, `version`, and `contentType`. Content type must be `Free`, `Open-source`, or `Copyrighted`. `author`, `emailAddress`, `tableOfContents`, and `description` are optional; a supplied email address must be valid. The folder identifier must be a safe, unique folder name.

The Knowledge Base metadata table is managed by Spring Data JPA. Tests use an in-memory H2 database, so PostgreSQL does not need to be running to execute `gradlew.bat test`.

User permissions are stored as `USER_ACCESS`, `ADMINISTRATOR_ACCESS`, or `NO_ACCESS`. An administrator can add or remove a permission with `PUT` or `DELETE /api/users/{userID}/permissions`, using a JSON body such as `{"permission":"USER_ACCESS"}`. Permission changes require HTTP Basic authentication with `ADMINISTRATOR_ACCESS`. Knowledge Base reads require `USER_ACCESS` or `ADMINISTRATOR_ACCESS`; Knowledge Base registration requires `ADMINISTRATOR_ACCESS`. HTTP Basic credentials must be sent with protected requests, so deploy behind HTTPS.

To provision the first administrator at startup, set both `APP_BOOTSTRAP_ADMIN_USER_ID` and `APP_BOOTSTRAP_ADMIN_PASSWORD`. The password is encoded before storage. Do not commit these environment values.

## CORS

The API allows the Angular development origin `http://localhost:4200` by default. Set `APP_CORS_ALLOWED_ORIGINS` to a comma-separated list of exact frontend origins when running in another environment.
