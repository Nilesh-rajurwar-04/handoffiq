# HandoffIQ Architecture & System Design Documentation

## 1. System Overview

**HandoffIQ** is an enterprise AI-powered knowledge preservation and employee handover platform designed to eradicate organizational brain-drain when employees change projects, take extended leave, miss critical meetings, or transition responsibilities.

The platform directly answers three fundamental operational questions:
1. **What happened previously?** (Structured meeting intelligence, decision logging, and historical context)
2. **What work is still pending?** (Active deliverables, blocked tasks, and carry-forward audits)
3. **What does the next employee need to know and do?** (AI-synthesized handover packages with explicit Handover Readiness Scores)

---

## 2. High-Level Architecture Diagram

```
                              +-------------------------------------------+
                              |         Client Browsers (Desktop/Mobile)   |
                              +-------------------------------------------+
                                                    |
                                            HTTPS / Port 443
                                                    v
                              +-------------------------------------------+
                              |           AWS Application Load Balancer   |
                              +-------------------------------------------+
                                                    |
                                                    v
                              +-------------------------------------------+
                              |         AWS EC2 Instance (Nginx Proxy)    |
                              |  - Port 443 -> SSL Termination             |
                              |  - Serves React + Vite Static SPA (/dist) |
                              |  - Reverse Proxies /api/ -> :8080         |
                              +-------------------------------------------+
                                                    |
                                           HTTP :8080 (Loopback)
                                                    v
+--------------------------------------------------------------------------------------------------+
| Spring Boot 3.2.5 Backend Runtime (Java 21, Amazon Corretto)                                     |
|                                                                                                  |
|  [Security & Observability Layer]                                                                |
|   - SecurityFilterChain: Stateless JWT 0.12.5 Authentication & BCrypt Password Encryption        |
|   - LoggingMdcFilter: Extracts/generates X-Correlation-Id, populates SLF4J MDC, tracks duration  |
|   - Role-Based Access Control (@PreAuthorize on ADMIN, MANAGER, EMPLOYEE)                        |
|                                                                                                  |
|  [REST Controllers]                                                                              |
|   - AuthController        - DashboardController    - MeetingController    - TaskController       |
|   - HandoverController    - AiController           - DocumentController   - NotificationController|
|                                                                                                  |
|  [Core Business Services]                                                                        |
|   - ReadinessScorerService: Special Standout 7-Factor Engine (0-100 Score & Mandatory Gates)     |
|   - MeetingService: Manages life cycles, notes, decisions, and action items                      |
|   - TaskService: Assignment, re-assignment, outcome tracking, and carry-forward lineage           |
|   - HandoverService: Orchestrates transfers, acknowledgements, and completion enforcement        |
|   - DocumentService: Metadata cataloging, chunking, and permission checks                        |
|                                                                                                  |
|  [Configurable AI Provider Engine]                                                               |
|   - AiServiceFactory: Resolves AI provider based on AI_PROVIDER environment variable             |
|   - Providers: GeminiAiService (Google) | OpenAiService (OpenAI) | RuleBasedFallbackAiService    |
|   - JSON Schemas: Strict structured outputs, temperature controls, prompt templates              |
|                                                                                                  |
|  [Data Persistence Layer]                                                                        |
|   - Spring Data JPA + Hibernate 6                                                                |
|   - HikariCP Connection Pooling                                                                  |
|   - Flyway Schema Migrations (V1 Initial Schema, V2 Seed Realistic Data)                         |
+--------------------------------------------------------------------------------------------------+
                                                    |
                                    +---------------+---------------+
                                    |                               |
                                    v                               v
            +-------------------------------+       +-------------------------------+
            |  AWS RDS MySQL 8.0 Instance   |       |  AWS S3 (or Local Store)      |
            |  - 24 Normalized Tables       |       |  - Document Binaries          |
            |  - Audit Trails & Lineages    |       |  - Handover PDF Exports       |
            +-------------------------------+       +-------------------------------+
```

---

## 3. Core Component Breakdown

### A. Frontend Layer (React 18 + Vite)
- **Styling Architecture**: Custom responsive design system using CSS Variables (`--primary-color: #4F46E5`, `--surface-color: #FFFFFF`, `--bg-primary: #F8FAFC`).
- **State & Authentication**: `AuthContext.jsx` provides global user session state, local storage persistence, and automatic 1-click quick login for demo roles.
- **Observability**: Interceptors automatically inject `X-Correlation-Id` into request headers.
- **Data Visualization**: Recharts provides responsive status pies, sprint velocity area charts, and department workload comparisons.
- **Specialized Widgets**:
  - `ReadinessScoreMeter`: Circular SVG radial gauge depicting 0-100 score and tier.
  - `ReadinessChecklistView`: 7-point interactive criteria breakdown.
  - `EditableActionItemCard` & `EditableDecisionCard`: In-place review of AI extractions before database commit.

### B. Backend Layer (Spring Boot 3.2.5 + Java 21)
- **Layered Architecture**: Controller -> Service -> Repository -> Entity.
- **Stateless Security**: Every request validates bearer token using `JwtAuthenticationFilter`. Expired or altered tokens yield standard `ErrorResponse` with HTTP 401.
- **Traceability & Structured Logging**: `LoggingMdcFilter` attaches `correlationId`, `userId`, `clientIp`, `httpMethod`, and `requestUri` to the MDC context. Output is recorded to `./logs/handoffiq.log` with daily rolling compression.
- **Global Error Handling**: `GlobalExceptionHandler` intercepts exceptions (`ResourceNotFoundException`, `HandoverIncompleteException`, `MethodArgumentNotValidException`, `AccessDeniedException`) and formats RFC 7807 compliant JSON errors.

---

## 4. Configurable AI Architecture

HandoffIQ treats LLM connectivity as an abstracted pluggable service.

```
                         [AiService] (High-level Interface)
                                   |
                         [AiServiceFactory]
                                   |
           +-----------------------+-----------------------+
           |                       |                       |
           v                       v                       v
  [GeminiAiService]        [OpenAiService]      [RuleBasedFallbackAiService]
  (Google Gemini Pro/Flash)  (GPT-4o / GPT-4o-mini) (Zero-credential local engine)
```

1. **Zero-Failure Fallback Guarantee**: If no API keys are provided or network errors occur, the backend transparently invokes `RuleBasedFallbackAiService`. It uses deterministic natural language heuristics to extract action items, owners, decisions, and dates so the user experience never breaks.
2. **Strict JSON Schema Enforcement**: AI prompts enforce structured JSON payloads matching DTO representations.
3. **Traceability Logging**: Every AI query, token count, duration, and response is recorded in the `ai_interaction_logs` table for compliance and auditability.

---

## 5. Security & Permission Architecture (RBAC)

| Role | Target Persona | Permissions & Scope |
|---|---|---|
| **`ROLE_ADMIN`** | System Admin, VP, HR Director | Full access across all departments, audit logs, employee directories, and documents. |
| **`ROLE_MANAGER`** | Engineering Manager, Tech Lead | Create meetings, initiate employee handovers, review AI extractions, reassign deliverables, sign off on handovers. |
| **`ROLE_EMPLOYEE`** | Software Engineer, Designer | View assigned tasks, record completion outcomes, run Catch-Up Briefs, acknowledge receipt of handovers, query AI Assistant. |
