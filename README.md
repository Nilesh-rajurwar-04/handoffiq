# HandoffIQ — AI-Powered Knowledge & Task Management Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-5.4-purple.svg)](https://vitejs.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20RDS%20%7C%20S3-yellow.svg)](https://aws.amazon.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

> **DigiPlus Technical Assessment: Production-Style Full-Stack Solution**  
> *Preventing the loss of organizational knowledge when employees change projects, go on leave, miss meetings, or transfer responsibilities.*

---

## 1. Project Objective & Core Value

Every organization faces critical knowledge drain during employee transitions, leaves of absence, sprint handovers, and missed meetings. Tribal knowledge is lost, tasks stall, and new assignees spend weeks context-switching.

**HandoffIQ** solves this problem by transforming unstructured meetings, discussions, decisions, tasks, and documents into structured institutional memory. The platform directly answers three vital questions:

1. **What happened previously?** (AI-structured meeting intelligence, historical architectural decisions, and runbooks)
2. **What work is still pending?** (Accountability-driven tasks, overdue alerts, and carry-forward audits)
3. **What does the next employee need to know and do?** (AI-synthesized handover packages with our **7-Factor Handover Readiness Score**)

---

## 2. Technology Stack

| Layer | Technologies | Key Features |
|---|---|---|
| **Frontend** | React 18, Vite, React Router DOM v6, Axios, Lucide React, Recharts | Deep Indigo SaaS theme (`#4F46E5`), responsive CSS variables, 1-Click Demo Login, live SVG gauges, interactive AI editable cards. |
| **Backend** | Java 21, Spring Boot 3.2.5, Spring Security 6, Spring Data JPA, Hibernate 6, Actuator | Stateless JJWT 0.12.5, BCrypt password hashing, Flyway migrations, SLF4J + Logback rolling file logging with MDC Correlation IDs. |
| **Database** | MySQL 8.0, Hibernate ORM, Flyway | 24 normalized 3NF relational tables, indexed foreign keys, pre-seeded with 11 realistic employees, 3 projects, 8 meetings, 20 tasks, 3 handovers, and 10 documents. |
| **AI Integration** | Configurable AI Engine (Google Gemini / OpenAI / Rule-Based Fallback) | Zero-credential resilience: runs seamlessly out-of-the-box with deterministic rule-based natural language parser, or connects directly to Gemini/OpenAI via environment variables. |
| **Deployment** | AWS EC2, AWS RDS MySQL, AWS S3, AWS CloudWatch, Nginx, Systemd | Complete production deployment scripts, Nginx reverse proxy configuration, systemd service units, and SSL setup. |

---

## 3. Standout Innovation: Handover Readiness Score (0–100)

HandoffIQ features a proprietary **Handover Readiness Score** algorithm implemented in `ReadinessScorerService.java`. Before a handover can be finalized, the platform evaluates 7 criteria:

$$\text{Readiness Score} = \sum_{i=1}^{7} (W_i \times C_i)$$

```
  0% ---------------- 50% ----------------- 75% ---------------- 100%
  [  Tier 1: Critical  ] [  Tier 2: In Progress ] [    Tier 3: Ready     ]
       Red Meter               Amber Meter             Green Meter
```

| Dimension | Criteria Evaluated | Weight | Mandatory Gate? |
|---|---|---|---|
| **C1** | All pending tasks have designated new owners | **20%** | **Yes** |
| **C2** | In-progress deliverables have documented status notes | **15%** | **Yes** |
| **C3** | Completed tasks have verifiable outcome descriptions | **15%** | No |
| **C4** | Critical blockers and dependencies are documented | **15%** | No |
| **C5** | Relevant architecture runbooks/docs are attached | **10%** | No |
| **C6** | Key decisions from previous meetings are linked | **15%** | No |
| **C7** | Receiving employee has formally acknowledged receipt | **10%** | **Yes** |

### Enforcement Guard
If a user attempts to complete a handover with a score below **75%** or with mandatory gates unfulfilled, the backend throws `HandoverIncompleteException` (HTTP 400), returning the exact missing requirements and preventing incomplete transitions.

---

## 4. Pre-Seeded Demonstration Credentials

HandoffIQ comes with 11 pre-seeded realistic users and a **1-Click Demo Login** bar on the sign-in page:

| Persona | Username | Password | Role | Description |
|---|---|---|---|---|
| **System Admin** | `admin` | `password123` | `ROLE_ADMIN` | Full organization control, user directory, system audit logs, and settings. |
| **Team Manager** | `manager` | `password123` | `ROLE_MANAGER` | Create meetings, initiate handovers, review AI extractions, and triage follow-ups. |
| **Senior Engineer** | `rahul.sharma` | `password123` | `ROLE_EMPLOYEE` | Developer transitioning modules, creating tasks, and recording outcomes. |
| **Database Engineer**| `priya.patel` | `password123` | `ROLE_EMPLOYEE` | Receiving employee on handovers, acknowledging receipt, and reviewing DB schemas. |
| **Cloud Architect** | `amit.verma` | `password123` | `ROLE_EMPLOYEE` | Cloud migration lead, author of AWS architecture runbooks. |
| **Product Designer**| `neha.gupta` | `password123` | `ROLE_EMPLOYEE` | UI/UX specifications, design system reviews. |

---

## 5. Quickstart Guide (Local Development)

### Prerequisites
- **Java 21 LTS** (`java -version`)
- **Node.js 18+** (`node -v`)
- **Maven 3.8+** (`mvn -v`)
- MySQL 8.0 (Optional; backend defaults to embedded H2 in MySQL mode for instant out-of-the-box execution)

### 1. Run the Backend
```bash
cd backend
mvn clean spring-boot:run
```
*The Spring Boot server starts at `http://localhost:8080`.*
- Swagger UI Documentation: `http://localhost:8080/swagger-ui/index.html`
- Health Check: `http://localhost:8080/actuator/health`

### 2. Run the Frontend
```bash
cd frontend
npm install
npm run dev
```
*The React application will launch at `http://localhost:5173`.*

---

## 6. AI Provider Configuration

HandoffIQ is designed with a pluggable AI architecture (`AiServiceFactory`). It runs out of the box with zero external API keys via `RuleBasedFallbackAiService`. To connect real LLMs, provide environment variables:

### Option A: Google Gemini (Recommended)
```bash
export AI_PROVIDER=GEMINI
export GEMINI_API_KEY="your-google-gemini-api-key"
export AI_MODEL_NAME="gemini-1.5-flash"
```

### Option B: OpenAI
```bash
export AI_PROVIDER=OPENAI
export OPENAI_API_KEY="your-openai-api-key"
export AI_MODEL_NAME="gpt-4o-mini"
```

---

## 7. Key REST API Catalog

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/auth/login` | Authenticate user & return JWT token | Public |
| `POST` | `/api/auth/register` | Register new user profile | Public |
| `GET` | `/api/dashboard/summary` | Retrieve KPI metrics & workload counts | Authenticated |
| `GET` | `/api/meetings` | List meetings with project & status filters | Authenticated |
| `POST` | `/api/meetings` | Schedule a new meeting | Manager / Admin |
| `POST` | `/api/ai/meeting-analysis` | Extract AI summary, action items & decisions | Authenticated |
| `POST` | `/api/ai/meeting-intelligence/approve` | Commit reviewed AI tasks & decisions to DB | Manager / Admin |
| `GET` | `/api/tasks` | List tasks with multi-field filtering | Authenticated |
| `POST` | `/api/tasks` | Create deliverable task | Authenticated |
| `POST` | `/api/tasks/{id}/outcome` | Complete task & record mandatory outcome | Assignee / Manager |
| `POST` | `/api/tasks/{id}/carry-forward` | Carry forward unresolved task to next meeting | Authenticated |
| `GET` | `/api/handovers` | List handovers with readiness scores | Authenticated |
| `POST` | `/api/handovers` | Initiate new employee handover package | Manager / Admin |
| `GET` | `/api/handovers/{id}/readiness` | Compute 7-factor Handover Readiness Score | Authenticated |
| `POST` | `/api/handovers/{id}/accept` | Formally acknowledge receipt of handover | Receiver / Manager |
| `POST` | `/api/handovers/{id}/complete` | Finalize handover (Enforces 75%+ score gate) | Manager / Admin |
| `POST` | `/api/ai/catch-up-brief` | Generate personalized catch-up digest | Authenticated |
| `POST` | `/api/ai/assistant` | Context-aware Q&A grounded in database records | Authenticated |
| `GET` | `/api/documents` | Catalog of knowledge documents & runbooks | Authenticated |
| `POST` | `/api/ai/knowledge-search` | Semantic RAG search across indexed documents | Authenticated |

---

## 8. AWS Cloud Deployment Architecture

Detailed instructions and automation scripts are located in `deployment/` and `docs/ARCHITECTURE.md`.

```
                    Internet
                       |
                  [Route 53] (DNS)
                       |
               [AWS ALB / HTTPS] (ACM SSL)
                       |
            +----------+----------+
            |                     |
     [EC2 Instance 1]      [EC2 Instance 2]  (Auto-Scaling Group)
     - Nginx (Reverse Proxy & SPA hosting)
     - Spring Boot 3 Service (Port 8080)
     - AWS CloudWatch Agent (Logs & Metrics)
            |                     |
            +----------+----------+
                       |
           +-----------+-----------+
           |                       |
     [AWS RDS MySQL 8]       [AWS S3 Bucket]
     (Multi-AZ Managed DB)   (Knowledge Runbooks)
```

### 1-Step Provisioning on EC2
```bash
chmod +x ./deployment/aws-setup.sh
sudo ./deployment/aws-setup.sh
```

---

## 9. Testing & Quality Verification

To execute the automated unit and integration test suite:
```bash
cd backend
mvn test
```
**Test Results**: 9 tests run, 0 failures, 0 errors.
- `ReadinessScorerTest`: Validates the 7-dimension scoring formula, weight constraints, and tier evaluation.
- `TaskServiceTest`: Validates task creation, outcome recording enforcement, and carry-forward lineage.
- `AiServiceTest`: Validates structured JSON schema output and deterministic fallback extraction.
- `HandoverWorkflowTest`: Validates that handovers below 75% score are strictly blocked with `HandoverIncompleteException`.

---

## 10. Repository Directory Structure

```
handoffiq/
├── backend/
│   ├── src/main/java/com/handoffiq/
│   │   ├── ai/              # Configurable AI Engine (Gemini, OpenAI, Fallback)
│   │   ├── config/          # Security, JWT, CORS, OpenAPI, Logging configuration
│   │   ├── controller/      # REST API Controllers (10 endpoints)
│   │   ├── dto/             # Request & Response Data Transfer Objects
│   │   ├── entity/          # JPA Domain Entities (24 relational entities)
│   │   ├── exception/       # GlobalExceptionHandler & custom exceptions
│   │   ├── repository/      # Spring Data JPA Repositories (16 interfaces)
│   │   └── service/         # Business logic services & ReadinessScorerService
│   ├── src/main/resources/
│   │   ├── db/migration/    # Flyway Migrations (V1 Schema, V2 Seed Data)
│   │   ├── application.properties
│   │   └── logback-spring.xml
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/      # Common, AI, Handover, and Layout widgets
│   │   ├── context/         # AuthContext with 1-click Demo Switcher
│   │   ├── pages/           # 20 Page screens (Meetings, Tasks, Handovers, etc.)
│   │   ├── services/        # Axios API client modules
│   │   ├── App.jsx          # Protected route definitions
│   │   └── index.css        # Responsive styling & variables
│   ├── package.json
│   └── vite.config.js
├── database/
│   ├── schema.sql           # Normalized MySQL 8.0 Schema (24 tables)
│   └── seed_data.sql        # Realistic organizational demonstration data
├── deployment/
│   ├── nginx.conf           # Production Nginx reverse proxy & SSL config
│   ├── handoffiq.service    # Systemd service unit for Spring Boot
│   └── aws-setup.sh         # Automated EC2 provisioning bash script
├── docs/
│   ├── ARCHITECTURE.md      # System architecture & component design
│   ├── DATABASE_ERD.md      # Relational entity-relationship diagram
│   └── HANDOVER_READINESS_SCORE.md # 7-criteria mathematical scoring engine
└── README.md
```

---

## 11. License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
