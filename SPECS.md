# TOGAFmate

**TOGAFmate** is an open-source Knowledge Bases application designed to help users organise, access, navigate, and read collections of knowledge documents through a web-based interface.

The application supports multiple Knowledge Bases, each containing Markdown documents and associated resources such as images. Knowledge Bases can contain learning material, technical documentation, reference information, and other authorised content.

TOGAFmate uses Angular for the frontend, Spring Boot with Spring Data JPA and Spring Security for the Java backend, PostgreSQL for metadata and access control, and persistent filesystem storage for document content. User passwords are stored as BCrypt hashes. The application uses a Docker-based deployment architecture on a VM server.

TOGAFmate is released under the **GNU General Public License v3.0 (GPL-3.0)**.

## 1. Objectives

TOGAFmate aims to provide:

- A central application for managing multiple **Architecture Methodologies and TOGAF as Knowledge Bases**.
- A consistent structure for all Knowledge Bases.
- A web-based interface for browsing and reading documents.
- ZIP-based Knowledge Base uploads.
- Persistent filesystem storage for Markdown documents and supporting resources.
- PostgreSQL storage for metadata and access permissions.
- Version identification for each Knowledge Base.
- Access control for readers and administrators.
- A secure mechanism for uploading, replacing, and removing Knowledge Bases.
- A Docker-based deployment model.
- An open-source platform that can be extended by the community.

## 2. Initial Features

### 2.1 Knowledge Base Management

TOGAFmate will support multiple independent Knowledge Bases.

Each Knowledge Base will have its own collection of documents and supporting resources.

Knowledge Bases may contain:

- Learning material.
- Technical documentation.
- Product documentation.
- Reference material.
- Standards and guidelines.
- Training material.
- Other authorised knowledge content.

Each Knowledge Base will have a title, version, description, content type, storage identifier, and associated document metadata.

Administrators will manage the Knowledge Base collection through the application.

### 2.2 Knowledge Base Content

A Knowledge Base will contain Markdown documents and any supporting resources required by those documents.

Supported content may include:

- Markdown files (`.md`).
- Images used by Markdown documents.
- Other supporting resources approved by the application.

The initial implementation will focus on Markdown documents and associated images.

The application will display Markdown content in a readable format through the Angular frontend.

### 2.3 Knowledge Base Navigation

Users will be able to browse the Knowledge Bases they are authorised to access.

The application will provide a Table of Contents for each Knowledge Base.

Users will be able to:

- Select a Knowledge Base.
- View its Table of Contents.
- Select a document.
- Read the document.
- Navigate to other documents within the same Knowledge Base.
- View associated images and other authorised resources.

The application will not require users to download an entire Knowledge Base to read an individual document.

### 2.4 Knowledge Base Import

Administrators will upload each Knowledge Base as a single ZIP file.

The backend will:

1. Receive the uploaded ZIP file.
2. Validate the archive and its contents.
3. Verify the required Knowledge Base structure.
4. Extract the files into the configured persistent filesystem storage.
5. Register the Knowledge Base and its documents in PostgreSQL.
6. Make the Knowledge Base available to authorised users.

The files will be extracted once during import. Subsequent document requests will read the extracted files rather than repeatedly extracting the ZIP archive.

### 2.5 Knowledge Base Administration

Administrators will be able to:

- Upload a Knowledge Base.
- Register its metadata.
- Replace an existing Knowledge Base version.
- Remove a Knowledge Base.
- Manage access permissions.
- View the registered documents and their metadata.

The application must validate changes before activating a new or replacement Knowledge Base.

Replacement and removal operations must avoid leaving database metadata and filesystem content inconsistent.

### 2.6 Search and Discovery

The initial application will provide basic Knowledge Base discovery.

Users should be able to locate Knowledge Bases and documents using available metadata, including:

- Knowledge Base title.
- Version.
- Description.
- Content type.
- Document name.
- Document title.
- Relevant tags, where supported.

More advanced full-text, semantic, and AI-assisted search may be considered for future releases.

## 3. Knowledge Base Content Types

Each Knowledge Base must specify one of the following content types.

### 3.1 Free

Content made available without a charge, subject to the applicable licence, copyright, and usage terms.

### 3.2 Open-source

Content distributed under an identified open-source licence or other applicable open licence.

The relevant licence must be identified where applicable.

### 3.3 Copyrighted

Content subject to copyright restrictions and the permissions granted by its copyright owner.

The administrator must ensure that the application is authorised to store and provide access to this content.

### 3.4 Content Type and Licensing

The content type describes the intended usage or distribution category of a Knowledge Base.

It does not replace the actual licence, copyright notice, or permission terms.

TOGAFmate must not assume that content is free to copy, modify, or redistribute simply because it is available without charge.

---

## 4. Application Architecture

TOGAFmate will use a **Docker-based deployment architecture** on a VM server. The application components will run in separate containers, with persistent storage for Knowledge Base documents and PostgreSQL data.

### 4.1 Architecture Components

- **Angular Frontend:** Provides the user interface for browsing, uploading, and managing Knowledge Bases.
- **Java Backend:** Exposes REST APIs, implements business logic, validates ZIP uploads, manages access control, and serves Knowledge Base content.
- **PostgreSQL Database:** Stores Knowledge Base metadata, document metadata, permissions, and optional audit records.
- **Knowledge Base Filesystem:** Stores extracted Markdown documents, images, supporting resources, `README.md`, and `TABLE_OF_CONTENTS.md`.
- **Docker:** Provides containerisation for the application components and supports consistent deployment.
- **Persistent Knowledge Base Storage:** Stores extracted Knowledge Base files independently of the backend container lifecycle. The preferred implementation is a named Docker volume. A mounted host directory on the VM server is an alternative when direct host access to files is required.
- **Persistent PostgreSQL Storage:** Stores PostgreSQL database files independently of the database container lifecycle. It uses a separate named Docker volume or an appropriately configured persistent host directory.
- **VM Server:** Hosts the Docker deployment and provides the underlying disk storage for persistent volumes or mounted directories.

### 4.2 Target Deployment Architecture

The following diagram illustrates the target architecture. The two persistent storage locations are separate so that Knowledge Base files and PostgreSQL data can be managed and backed up independently.

```mermaid
flowchart TB
    User["User / Administrator"]

    subgraph VM["VM Server"]
        subgraph Docker["Docker-Based Deployment"]
            Frontend["Angular Frontend Container"]
            Backend["Java Backend Container"]
            Database["PostgreSQL Container"]

            KBVolume[("Persistent Knowledge Base Volume")]
            DBVolume[("Persistent PostgreSQL Data Volume")]

            Frontend -->|"REST API requests"| Backend
            Backend -->|"Metadata queries"| Database
            Backend -->|"Read and write files"| KBVolume
            Database -->|"Database files"| DBVolume
        end

        HostStorage["Optional mounted host directories"]
    end

    User --> Frontend
```

The diagram shows the logical deployment. In the preferred configuration, named Docker volumes provide persistent storage. Alternatively, the deployment can use mounted host directories on the VM server. The application must use one configured persistent storage arrangement for each data store.

### 4.3 Knowledge Base Upload and Storage

1. An administrator uploads a Knowledge Base as a ZIP file through the Angular frontend.
2. The Java backend validates the archive, including its paths, file types, structure, and size limits.
3. The backend extracts the archive into a temporary directory and validates the extracted content.
4. The backend activates the validated content in the configured persistent Knowledge Base storage location.
5. The backend stores the associated metadata and permissions in PostgreSQL.
6. Users access authorised Knowledge Base content through the REST API.

Knowledge Base documents are stored on the persistent filesystem, not inside the PostgreSQL database. PostgreSQL stores the associated metadata.

The configured storage root is controlled by the server and cannot be overridden by the uploaded archive.

### 4.4 Deployment Principles

- Deploy the Angular frontend, Java backend, and PostgreSQL as separate Docker containers.
- Keep Knowledge Base documents on persistent storage independent of the backend container lifecycle.
- Keep PostgreSQL data on separate persistent storage independent of the database container lifecycle.
- Use a named Docker volume for Knowledge Base storage by default.
- Use a mounted host directory instead when administrators require direct access to Knowledge Base files on the VM server.
- Ensure the backend is the authorised access point for protected Knowledge Base content.
- Configure database credentials and other secrets securely; do not hard-code them into application source code.
- Ensure the deployment can be recreated consistently across development, testing, and production environments.
- Ensure that container replacement or recreation does not unintentionally delete persistent data.

### 4.5 Frontend Location, Experience, and Themes

- The Angular frontend source code must be located in the repository's `frontend/` directory, alongside `backend/`.
- The interface must use a clean, uncluttered corporate style with clear headings, readable typography, generous spacing, and familiar navigation.
- The default visual direction is a light interface with warm-white or light-gray surfaces, dark-charcoal text, and a restrained muted-blue or teal accent for links and primary actions.
- Provide three selectable visual themes: **Light**, **Gray**, and **Black**. Light is the default theme. All themes must maintain readable text contrast and accessible controls.
- Use CSS styling and theme variables so the corporate look and feel can be applied consistently and adjusted without changing application behavior.
- The frontend is a separately deployable Docker application component and communicates with the Java backend over the REST API.

### 4.6 Frontend Authentication and Permissions

- Users sign in with their user ID and password using HTTP Basic authentication. The backend's logon operation returns the user's permissions after validating the credentials.
- The frontend uses HTTP Basic credentials for protected API requests and uses the returned permissions to present the pages and actions available to that user. The API does not use an application session or bearer-token flow in the initial implementation.
- Users with `USER_ACCESS` may access reader pages and perform read-only operations on Knowledge Bases and their content.
- Users with `ADMINISTRATOR_ACCESS` may access reader pages and administrative pages and perform the administrative operations allowed by the backend.
- Users without the required permission must not be shown protected pages or actions. The backend must independently enforce every permission check, as specified in Section 10.3.
- The frontend must provide a sign-out action that clears the in-memory authentication state and clear feedback for failed authentication or denied access.
- Production deployments must use HTTPS for frontend and API traffic so credentials and protected content are encrypted in transit. Local development may use HTTP on localhost. Docker deployment must provide HTTPS at the application entry point, such as through a TLS-terminating reverse proxy; containerisation alone does not provide HTTPS.
- The frontend must not persist passwords or HTTP Basic credentials in browser storage. Credentials should be held in memory only while the user is signed in.

### 4.7 Reader and Administrator User Journeys

#### Reader journey

1. The user signs in with their user ID and password.
2. The application displays the Knowledge Bases that the user's permissions allow them to access. The list should show the title, version, and description where available, and provide basic metadata search.
3. The user selects a Knowledge Base and opens its reading view.
4. The reading view presents the Table of Contents alongside the selected Markdown document. If no Table of Contents is supplied, the application presents navigation generated from registered document metadata.
5. The user selects other documents from the navigation and continues reading. The user can return to the Knowledge Base list and sign out at any time.

#### Administrator journey

1. The administrator signs in with their user ID and password and receives the reader experience plus an Admin area.
2. In the Admin area, the administrator registers a Knowledge Base or uploads its ZIP archive, reviews validation feedback, and sees confirmation when it is accepted.
3. The administrator views registered Knowledge Bases and can select one to replace or remove it. The application requests confirmation before replacement or removal and displays the operation result.
4. The administrator finds a user and grants or revokes reader or administrator permissions. The application confirms permission changes and reports success or failure.

The user journeys describe the intended frontend experience. Upload, document retrieval, replacement, and removal require corresponding backend API operations before those steps can work end to end.

### 4.8 Container Build and Release Stages

Containerisation will be delivered in two stages.

#### Stage 1: Build and publish application images

- Build and publish separate frontend and backend images. Each application image must have its own Dockerfile in its application directory: `frontend/Dockerfile` and `backend/Dockerfile`.
- Build the Angular frontend with Node.js and serve its production files from an Alpine-based runtime image.
- Build and run the Java backend on Ubuntu 24.04 LTS with Java 21.
- Label and publish the first-stage images with version `1.0`, using separate Docker Hub repositories, for example `<namespace>/togafmate-frontend:1.0` and `<namespace>/togafmate-backend:1.0`.
- The project maintainer builds and publishes the images to Docker Hub. Application secrets must not be included in either image.
- Stage 1 does not provide a Compose deployment or a PostgreSQL container. The backend requires its database connection and bootstrap administrator configuration through runtime environment variables when run.
- The frontend image must serve the Angular single-page application and forward `/api` requests to the backend service named `backend` on port `8080`, ready for the shared network defined in Stage 2.

#### Stage 2: Compose deployment

- Add a root-level `compose.yaml` to run the frontend, backend, and PostgreSQL as separate services.
- Connect the services on a shared Docker network. The frontend must be able to reach the backend by the service name `backend`; the backend must reach PostgreSQL by its Compose service name.
- Use the official PostgreSQL image and pin it to a supported major version. Do not use the floating `latest` tag for deployments.
- Persist PostgreSQL data in a volume separate from persistent Knowledge Base file storage.
- Provide deployment-specific secrets and configuration at runtime. A local `.env` file may supply Compose variables but must be excluded from version control and must not be copied into application images. Commit a `.env.example` containing variable names and placeholders only. Production deployments should inject secrets through the deployment environment or a secrets manager.
- Production access must use HTTPS for frontend and API traffic, with TLS provided by the deployment's HTTPS entry point. Docker or Compose alone does not enable TLS.

Stage 2 Compose configuration, PostgreSQL image version, persistent volumes, and runtime secret delivery are not part of Stage 1 image publishing.

---

## 5. Technology Stack

TOGAFmate will use a Docker-based containerised deployment architecture.

| Component | Technology |
|---|---|
| Frontend | Angular |
| Backend | Java |
| API | REST |
| Database | PostgreSQL |
| Document storage | Persistent server filesystem |
| Preferred Knowledge Base storage | Named Docker volume |
| Alternative Knowledge Base storage | Mounted host directory on the VM server |
| PostgreSQL storage | Separate persistent Docker volume or mounted host directory |
| Import format | ZIP |
| Document format | Markdown |
| Build | Gradle |
| Testing | JUnit |
| Code coverage | JaCoCo |
| Code quality | SonarQube |
| Containerisation | Docker |
| IDE | IntelliJ IDEA Community Edition |

The initial implementation will be designed to run locally using Docker and will also support development directly from IntelliJ IDEA Community Edition.

---

## 6. Knowledge Base Structure and Storage

TOGAFmate uses Docker containerisation for application deployment. Knowledge Base files are stored in persistent storage on the VM server and accessed by the application through a Docker volume or a mounted host directory.

A named Docker volume is the preferred option when administrators do not require direct access to individual files on the VM server.

### 6.1 Knowledge Base Metadata Fields

When an administrator uploads a Knowledge Base ZIP file, the application captures the following metadata.

| Field | Required / Optional | Description |
|---|---|---|
| `title` | Required | The title of the Knowledge Base. |
| `folder_name_version` | Required | The unique folder identifier derived from the Knowledge Base title and version. |
| `version` | Required | The version number of the Knowledge Base. |
| `author` | Optional | The author of the Knowledge Base. |
| `email_address` | Optional | The author's email address. |
| `Table_Of_Contents` | Optional | The Table of Contents content or resource reference, if provided. |

The application must validate all required fields before registering and activating a Knowledge Base.

Optional fields may be left blank. Their absence must not prevent an otherwise valid Knowledge Base from being uploaded.

The `folder_name_version` field identifies the Knowledge Base directory within the configured storage location. It must conform to the application's naming rules and must not contain an arbitrary filesystem path.

### 6.2 Knowledge Base README.md

Each Knowledge Base ZIP file must contain a `README.md` file describing the Knowledge Base.

The README must use a consistent structure so that TOGAFmate can validate and register the Knowledge Base metadata.

The following fields must be represented in the README.

| README field | Required / Optional |
|---|---|
| Title | Required |
| Folder Name and Version (`folder_name_version`) | Required |
| Version | Required |
| Author | Optional |
| Email Address | Optional |
| Table of Contents (`Table_Of_Contents`) | Optional |

#### 6.2.1 README Field Definitions

- **Title — Required:** The display title of the Knowledge Base.
- **Folder Name and Version — Required:** The identifier used for the Knowledge Base directory on persistent storage.
- **Version — Required:** The version number of the Knowledge Base.
- **Author — Optional:** The name of the person or organisation responsible for the content.
- **Email Address — Optional:** The author's contact email address.
- **Table of Contents — Optional:** A Table of Contents supplied with the Knowledge Base. If omitted, TOGAFmate may provide navigation using registered document metadata.

The README field names and their interpretation must be consistent across all Knowledge Bases.

#### 6.2.2 Example README.md

The following example shows all six fields, including the optional fields.

```markdown
# TOGAF Architecture Guide v1.0

## Title

TOGAF Architecture Guide

## Folder Name and Version

TOGAF_Architecture_Guide_v1.0

## Version

1.0

## Author (Optional)

Architecture Documentation Team

## Email Address (Optional)

architecture@example.com

## Table_Of_Contents (Optional)

TABLE_OF_CONTENTS.md
```

The example email address is illustrative and must be replaced with a valid address if an author email is supplied.

If the author, email address, or Table of Contents is not available, its field may be omitted or left blank according to the agreed README validation rules.

### 6.3 Knowledge Base Content Structure

Each Knowledge Base is stored in its own directory on the persistent filesystem. The directory name is derived from `folder_name_version`.

Example:

```text
KnowledgeBaseStorage/
└── TOGAF_Architecture_Guide_v1.0/
    ├── README.md
    ├── TABLE_OF_CONTENTS.md
    ├── Chapter_01.md
    ├── Chapter_02.md
    └── resources/
        ├── diagrams/
        └── images/
```

The `KnowledgeBaseStorage` directory is illustrative. In deployment, it maps to a named Docker volume or a mounted host directory on the VM server.

The actual filesystem path is configured by the deployment administrator. The uploaded ZIP archive cannot override the configured storage root.

### 6.4 ZIP Upload and Registration Process

When an administrator uploads a Knowledge Base ZIP file, the backend will:

1. Receive the ZIP file through the Angular frontend.
2. Validate the archive, including its file paths, file types, archive size, file count, and total extracted size.
3. Extract the archive into a temporary directory.
4. Locate and validate the `README.md` file.
5. Read and validate the required metadata fields: `title`, `folder_name_version`, and `version`.
6. Read the optional metadata fields: `author`, `email_address`, and `Table_Of_Contents`, when supplied.
7. Validate the extracted document structure and supporting resource paths.
8. Confirm that the Knowledge Base folder identifier conforms to the application's naming rules.
9. Activate the validated content in the configured persistent storage location.
10. Register the Knowledge Base metadata, document metadata, and permissions in PostgreSQL.
11. Make the Knowledge Base available to authorised users.

The backend must not activate an invalid or incomplete Knowledge Base.

If an import fails, temporary files must be cleaned up and the existing active Knowledge Base must remain usable.

### 6.5 Persistent Storage

- **Docker containerisation:** The application components run in Docker containers on the VM server.
- **Persistent Knowledge Base storage:** Extracted Knowledge Base files are stored in a named Docker volume or a mounted host directory on the VM server.
- **Preferred option:** Use a named Docker volume when direct host access to files is not required.
- **Alternative option:** Use a mounted host directory when administrators need direct access to the stored files.
- **Database storage:** PostgreSQL stores Knowledge Base metadata, document metadata, permissions, and optional audit records.
- **Separate storage:** PostgreSQL data and Knowledge Base files must use separate persistent storage locations.
- **Data persistence:** Knowledge Base files and database data must survive container restarts, upgrades, replacement, and recreation.
- **Storage configuration:** The deployment administrator configures the storage locations. Uploaded archives cannot override these locations.

The application must not store Knowledge Base documents only in a container's writable layer.

### 6.6 Backup and Recovery

- Backups must cover both PostgreSQL data and the persistent Knowledge Base filesystem.
- Backup and recovery procedures must maintain consistency between database metadata and stored files.
- Backups must be stored in a suitable separate location so that loss of the VM or its disk does not destroy both the live data and its backups.
- Recovery procedures must restore PostgreSQL and the corresponding Knowledge Base files to a mutually consistent state.
- Replacement and deletion operations must avoid leaving database metadata pointing to missing files or leaving obsolete files incorrectly registered as active.

Backup schedules, retention periods, and detailed recovery procedures will be defined during deployment design.

---

## 7. Knowledge Base README.md

Each Knowledge Base ZIP file must contain a `README.md` file in its root directory.

The `README.md` describes and identifies the Knowledge Base. It must contain the three required fields and may contain the three optional fields defined below.

### 7.1 README.md Fields

| Field | Required? | Description |
|---|---|---|
| `title` | **Required** | The display title of the Knowledge Base. |
| `folder_name_version` | **Required** | The folder identifier used to store the Knowledge Base files. |
| `version` | **Required** | The version number of the Knowledge Base. |
| `author` | Optional | The name of the author or responsible team. |
| `email_address` | Optional | The contact email address. |
| `Table_Of_Contents` | Optional | A reference to the Table of Contents resource, if provided. |

The field names must be used consistently across all Knowledge Bases.

Optional fields may be omitted or left blank. The backend must reject an upload if any required field is missing or invalid.

### 7.2 Title

The `title` field identifies the Knowledge Base and is displayed to users.

Example:

```text
TOGAF Architecture Guide
```

The title does not have to contain the version number because the version is stored separately.

### 7.3 Folder Name and Version

The `folder_name_version` field identifies the directory in which the Knowledge Base content is stored.

Example:

```text
TOGAF_Architecture_Guide_v1.0
```

The `version` field identifies the version of the Knowledge Base.

Example:

```text
1.0
```

The folder identifier must follow the application's naming rules and must not contain an arbitrary filesystem path. The backend determines the actual storage location from its configuration.

### 7.4 Author and Email Address (Optional)

The `author` field identifies the person or team responsible for the Knowledge Base.

The `email_address` field provides an optional contact address.

Neither field is mandatory. The Knowledge Base can be uploaded without an author or email address.

### 7.5 Table of Contents (Optional)

The `Table_Of_Contents` field identifies the Table of Contents resource when one is supplied.

Example:

```text
TABLE_OF_CONTENTS.md
```

The field is optional. If it is omitted or blank, TOGAFmate must still provide document navigation, using an application-defined fallback such as generating navigation from the registered documents and their metadata.

If a Table of Contents resource is specified, the backend must validate its reference and ensure that it points to an authorised resource within the Knowledge Base.

### 7.6 Example README.md

The following example includes all six fields. The three optional fields are explicitly labelled.

```markdown
# TOGAF Architecture Guide

## title
TOGAF Architecture Guide

## folder_name_version
TOGAF_Architecture_Guide_v1.0

## version
1.0

## author (Optional)
Architecture Documentation Team

## email_address (Optional)
architecture@example.com

## Table_Of_Contents (Optional)
TABLE_OF_CONTENTS.md
```

The example email address is illustrative.

### 7.7 Backend Validation

During ZIP upload, the backend must:

1. Locate and read the `README.md` file.
2. Validate that `title`, `folder_name_version`, and `version` are present and valid.
3. Read `author`, `email_address`, and `Table_Of_Contents` when supplied.
4. Validate the folder identifier and any Table of Contents reference.
5. Reject invalid uploads before activating the Knowledge Base.
6. Register the validated metadata in PostgreSQL.
7. Store the extracted documents and resources in the configured persistent filesystem storage.

The metadata fields defined in this chapter must be consistent with the Knowledge Base metadata fields defined in Chapter 6.

---

## 8. Table of Contents

Each Knowledge Base will have a separate `TABLE_OF_CONTENTS.md` resource.

The Table of Contents will provide the navigation structure for the documents within that Knowledge Base.

### 8.1 Table of Contents Responsibilities

The Table of Contents will allow users to:

- See the available documents.
- Identify document titles.
- Select a document to read.
- Navigate between documents.
- Access supporting resources where authorised.

The Table of Contents is a navigation mechanism. It is not a second copy of the document content.

### 8.2 Table of Contents Data

The application will use the Knowledge Base files and document metadata to provide navigation.

The initial design will follow this process:

1. PostgreSQL stores metadata for each registered document.
2. The backend identifies the documents belonging to a Knowledge Base.
3. The backend provides the navigation data through a REST API.
4. Angular displays the Table of Contents.
5. The user selects a document.
6. The backend checks authorisation.
7. The backend retrieves and returns the requested Markdown content.

### 8.3 Table of Contents Implementation

Two implementation options have been identified.

**Option A — Angular component**

The backend provides document metadata through a REST API. Angular builds and displays the Table of Contents dynamically.

**Option B — HTML resource**

The Knowledge Base contains an HTML Table of Contents that the frontend displays or integrates into its navigation.

The implementation approach will be decided during detailed design.

Regardless of the chosen approach, the backend must enforce authorisation whenever a document or supporting resource is requested.

---

## 9. Common Storage and Processing

All Knowledge Bases will use the same storage and processing mechanism.

### 9.1 Import Process

The import process will:

1. Receive a ZIP file from an administrator.
2. Validate the archive.
3. Check the required Knowledge Base structure.
4. Validate the README and required metadata.
5. Check file types and archive limits.
6. Extract the files into a temporary directory.
7. Validate the extracted content and resource paths.
8. Move or activate the validated content in the configured persistent storage directory.
9. Register the Knowledge Base and document metadata in PostgreSQL.
10. Make the Knowledge Base available according to its access permissions.

The application must avoid exposing partially uploaded or invalid Knowledge Bases.

The import process must handle failures without corrupting an existing active Knowledge Base or leaving inconsistent database metadata.

### 9.2 Document Retrieval

When a user requests a document:

1. The backend identifies the requested Knowledge Base.
2. The backend checks the user's access permissions.
3. The backend validates the requested document identifier or path.
4. The backend locates the file within the configured Knowledge Base directory.
5. The backend reads the document from the filesystem.
6. The backend returns the content to the frontend.

The backend must not permit a request to access files outside the authorised Knowledge Base.

### 9.3 Filesystem and Database Responsibilities

The filesystem stores the actual content.

PostgreSQL stores metadata and application information.

| Information | Storage location |
|---|---|
| Knowledge Base Markdown files | Persistent filesystem |
| Images and supporting resources | Persistent filesystem |
| Knowledge Base README | Persistent filesystem |
| Table of Contents resource | Persistent filesystem |
| Knowledge Base title and version metadata | PostgreSQL |
| Document metadata | PostgreSQL |
| Access permissions | PostgreSQL |
| Administrative audit information | PostgreSQL, where required |

The application will not maintain duplicate copies of the Markdown documents in PostgreSQL.

The Knowledge Base filesystem and PostgreSQL data will use separate persistent storage locations.

### 9.4 Replacing a Knowledge Base

TOGAFmate must allow an administrator to replace an existing Knowledge Base with a new version of the same Knowledge Base.

The following rules apply:

- The **title may remain the same** when a new version is uploaded.
- The **version must be different** from the version being replaced.
- The new version must be validated before it replaces the existing version.
- The application must not activate an incomplete or invalid upload.
- If validation or activation fails, the existing Knowledge Base must remain available and unchanged.
- The application must prevent accidental replacement of the wrong Knowledge Base.

**Example:**

| Existing Knowledge Base | Uploaded Knowledge Base | Result |
|---|---|---|
| TOGAF Architecture Guide, version 1.0 | TOGAF Architecture Guide, version 1.1 | Allowed |
| TOGAF Architecture Guide, version 1.0 | TOGAF Architecture Guide, version 2.0 | Allowed |
| TOGAF Architecture Guide, version 1.0 | TOGAF Architecture Guide, version 1.0 | Rejected as a replacement because the version is unchanged |
| TOGAF Architecture Guide, version 1.0 | Azure Architecture Guide, version 1.0 | Treated as a different Knowledge Base, not a replacement |

The replacement operation must identify the existing Knowledge Base and verify that the uploaded version differs from its current version.

The implementation must also define whether previous versions are retained or removed after a successful replacement. This policy will be decided during detailed design.

### 9.5 Removing a Knowledge Base

When an administrator removes a Knowledge Base, the application must coordinate the removal of its metadata and stored files.

The implementation must define how it handles failures during removal so that the database and filesystem do not become inconsistent.

Where appropriate, the application should use a controlled deletion or recovery mechanism rather than immediately and irreversibly deleting content.

The detailed deletion and recovery policy will be decided during detailed design.

---

## 10. Access Control and Security

TOGAFmate will distinguish between readers and administrators.

### 10.1 Readers

Readers will be able to:

- Browse Knowledge Bases they are authorised to access.
- View their Tables of Contents.
- Read authorised Markdown documents.
- Access authorised supporting resources.

Readers will not be able to upload, replace, or remove Knowledge Bases or change permissions.

### 10.2 Administrators

Administrators will be able to:

- Upload Knowledge Bases.
- Manage Knowledge Base metadata.
- Replace or remove Knowledge Bases.
- Manage access permissions.
- Maintain the Knowledge Base collection.
- Review administrative operations where audit logging is implemented.

Administrative functions must be protected against unauthorised use.

### 10.3 Server-Side Authorisation

The backend must enforce authorisation for every protected operation.

This includes:

- Listing restricted Knowledge Bases.
- Reading document content.
- Accessing images and other resources.
- Uploading ZIP files.
- Replacing Knowledge Bases.
- Removing Knowledge Bases.
- Changing permissions.

Hiding links or buttons in Angular is not sufficient security.

The backend must independently verify the user's permissions before performing the requested operation.

### 10.4 ZIP Upload Security

Uploaded ZIP files must be treated as untrusted input.

The backend must protect against:

- ZIP path traversal, commonly called Zip Slip.
- Attempts to write outside the configured storage directory.
- Absolute paths and unsafe path components.
- Unexpected file types.
- Excessive archive sizes.
- Excessive numbers of files.
- Excessive extracted sizes.
- Malformed or corrupted archives.
- Unexpected directory structures.
- Unsafe references to supporting resources.

The backend must use a server-configured storage root and must not trust filesystem paths supplied by the archive.

### 10.5 Safe Activation

A Knowledge Base must not become available until validation and extraction have completed successfully.

The application should extract content to a temporary directory and activate it only after validation.

If an import fails, the application must clean up temporary files and avoid leaving inconsistent metadata or partially active content.

Replacement and removal operations must also be designed to preserve database and filesystem consistency when failures occur.

---

## 11. Development Principles

TOGAFmate will follow these development principles:

- Open-source development under GPL-3.0.
- API-first development.
- Provide Swagger API documentation to support testing the backend.
- Separation of frontend and backend.
- Separation of metadata from document content.
- PostgreSQL for structured metadata and permissions.
- Persistent filesystem storage for Markdown and supporting resources.
- Separate persistent storage for Knowledge Base files and PostgreSQL data.
- Consistent structure for all Knowledge Bases.
- ZIP as the initial import format.
- Secure document access through the backend.
- Server-side authorisation for all protected operations.
- Coordinated backup and recovery for database metadata and filesystem content.
- Safe replacement and removal of Knowledge Bases.
- Automated testing.
- Continuous code-quality analysis.
- Containerised deployment using Docker.
- Prefer proven technologies over reinventing functionality.
- Keep the platform extensible.
- Maintain clear and complete documentation.

---

## 12. Initial Project Scope

The initial release will concentrate on establishing the core Knowledge Bases application.

The scope includes:

1. Angular web interface.
2. Java backend.
3. REST API.
4. PostgreSQL database.
5. Knowledge Base metadata management.
6. Document metadata management.
7. ZIP upload and validation.
8. Safe extraction to persistent server filesystem storage.
9. Knowledge Base folder structure validation.
10. README validation.
11. Table of Contents display.
12. Markdown document retrieval and display.
13. Supporting image retrieval.
14. Reader and administrator roles.
15. Access permission management.
16. Server-side authorisation.
17. Safe replacement and removal of Knowledge Bases.
18. Docker containerisation.
19. Persistent storage configuration for Knowledge Base files and PostgreSQL data.
20. Backup and recovery procedures for persistent data.
21. Automated testing.
22. JaCoCo code coverage.
23. SonarQube integration.

Additional capabilities, including advanced search and AI-assisted knowledge discovery, may be introduced in later releases.

### 12.1 Excluded Applications and Responsibilities

TOGAFmate is the **Knowledge Bases application**.

The following capabilities belong to separate programs and are not part of this application's scope:

- A TOGAF Architecture Repository for managing architecture artefacts and their governance workflows.
- An Architecture Decision Repository for managing architecture decisions and their lifecycle.

TOGAFmate will not implement those repositories as modules within this application.

Its responsibility is to manage Knowledge Bases and provide secure access to their content.

---

## 13. Project Status

**Status: PLANNING**

TOGAFmate is currently in the planning and initial design phase.

The core technology choices, Docker-based deployment architecture, and Knowledge Base storage approach have been identified.

Detailed design is still required for areas such as:

- The final Table of Contents implementation.
- Authentication hardening and deployment configuration.
- Database schema.
- REST API contracts.
- Knowledge Base replacement and deletion policies.
- Persistent volume and host-directory configuration.
- Backup schedules, retention, and recovery procedures.
- Deployment configuration for the VM server.

The project will progress through requirements definition, detailed design, implementation, testing, and deployment.

---

## 14. Contributing

TOGAFmate is intended to be an open-source project, and contributions are welcome.

Contributors will be able to participate in:

- Application development.
- Backend development.
- Frontend development.
- Database design.
- Security and access control.
- User interface and user experience.
- Knowledge Base examples.
- Documentation.
- Testing.
- Feature development.

Contribution guidelines will be added as the project matures.

---

## 15. Security

Since this software is offered "Free without Warranty" or "Voetstoots" and the source code is provided to you, you are welcome to make changes to this code to meet your own security requirements

---

## 16. License

TOGAFmate is licensed under the **GNU General Public License version 3.0 (GPL-3.0)**.

The software may be used, modified, and distributed in accordance with the terms of that licence.

When distributing the software or derivative works, the applicable GPL-3.0 requirements must be followed.

The licence for the TOGAFmate application does not automatically determine the licence or usage rights of documents uploaded into its Knowledge Bases. Knowledge Base administrators remain responsible for ensuring that uploaded content may lawfully be stored and made available to its intended users.

For the complete licence terms, see the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.htm).

The repository should include the corresponding `LICENSE` file.
