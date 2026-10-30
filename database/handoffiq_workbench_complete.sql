/*
  HandoffIQ — Complete MySQL Workbench Script
  Creates database, schema, and demonstration data.
*/

CREATE DATABASE IF NOT EXISTS handoffiq CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE handoffiq;

-- =============================================================================
-- HandoffIQ Relational Database Schema (MySQL 8.0 Compatible)
-- Enterprise AI-Powered Knowledge & Task Continuity Platform
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS ai_interaction_logs;
DROP TABLE IF EXISTS activity_logs;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS document_chunks;
DROP TABLE IF EXISTS knowledge_documents;
DROP TABLE IF EXISTS handover_acknowledgements;
DROP TABLE IF EXISTS handover_documents;
DROP TABLE IF EXISTS handover_tasks;
DROP TABLE IF EXISTS handovers;
DROP TABLE IF EXISTS task_carry_forward_history;
DROP TABLE IF EXISTS task_outcomes;
DROP TABLE IF EXISTS task_status_history;
DROP TABLE IF EXISTS task_comments;
DROP TABLE IF EXISTS task_assignments;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS decisions;
DROP TABLE IF EXISTS meeting_notes;
DROP TABLE IF EXISTS meeting_participants;
DROP TABLE IF EXISTS meetings;
DROP TABLE IF EXISTS project_members;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS departments;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. Departments
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Roles
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    department_id BIGINT,
    designation VARCHAR(100),
    avatar_url VARCHAR(255),
    phone VARCHAR(30),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL,
    INDEX idx_users_email (email),
    INDEX idx_users_department (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Projects
CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(30) NOT NULL UNIQUE,
    description TEXT,
    department_id BIGINT,
    lead_id BIGINT,
    status VARCHAR(30) DEFAULT 'ACTIVE',
    start_date DATE,
    target_end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_projects_department FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL,
    CONSTRAINT fk_projects_lead FOREIGN KEY (lead_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_projects_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Project Members
CREATE TABLE project_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_in_project VARCHAR(100) DEFAULT 'Contributor',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_pm_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_project_user UNIQUE (project_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Meetings
CREATE TABLE meetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    project_id BIGINT NOT NULL,
    created_by_id BIGINT NOT NULL,
    scheduled_start DATETIME NOT NULL,
    scheduled_end DATETIME,
    status VARCHAR(30) DEFAULT 'SCHEDULED',
    location VARCHAR(255),
    agenda TEXT,
    raw_notes LONGTEXT,
    ai_summary LONGTEXT,
    key_discussion_points LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_meetings_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_meetings_creator FOREIGN KEY (created_by_id) REFERENCES users (id),
    INDEX idx_meetings_project (project_id),
    INDEX idx_meetings_status (status),
    INDEX idx_meetings_date (scheduled_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Meeting Participants
CREATE TABLE meeting_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    attendance_status VARCHAR(30) DEFAULT 'INVITED',
    notes VARCHAR(255),
    CONSTRAINT fk_mp_meeting FOREIGN KEY (meeting_id) REFERENCES meetings (id) ON DELETE CASCADE,
    CONSTRAINT fk_mp_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_meeting_participant UNIQUE (meeting_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Meeting Notes
CREATE TABLE meeting_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    note_type VARCHAR(50) DEFAULT 'RAW',
    content LONGTEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mn_meeting FOREIGN KEY (meeting_id) REFERENCES meetings (id) ON DELETE CASCADE,
    CONSTRAINT fk_mn_author FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Decisions
CREATE TABLE decisions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    meeting_id BIGINT,
    title VARCHAR(255) NOT NULL,
    context TEXT,
    rationale TEXT,
    status VARCHAR(30) DEFAULT 'APPROVED',
    decided_by_id BIGINT,
    decided_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_decisions_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_decisions_meeting FOREIGN KEY (meeting_id) REFERENCES meetings (id) ON DELETE SET NULL,
    CONSTRAINT fk_decisions_user FOREIGN KEY (decided_by_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_decisions_project (project_id),
    INDEX idx_decisions_meeting (meeting_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Tasks
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description LONGTEXT,
    project_id BIGINT NOT NULL,
    assigned_to_id BIGINT,
    created_by_id BIGINT NOT NULL,
    priority VARCHAR(30) DEFAULT 'MEDIUM',
    status VARCHAR(30) DEFAULT 'TODO',
    start_date DATE,
    due_date DATE,
    related_meeting_id BIGINT,
    related_decision_id BIGINT,
    completion_notes TEXT,
    completion_outcome VARCHAR(255),
    completed_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_assignee FOREIGN KEY (assigned_to_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_tasks_creator FOREIGN KEY (created_by_id) REFERENCES users (id),
    CONSTRAINT fk_tasks_meeting FOREIGN KEY (related_meeting_id) REFERENCES meetings (id) ON DELETE SET NULL,
    CONSTRAINT fk_tasks_decision FOREIGN KEY (related_decision_id) REFERENCES decisions (id) ON DELETE SET NULL,
    INDEX idx_tasks_project (project_id),
    INDEX idx_tasks_assignee (assigned_to_id),
    INDEX idx_tasks_status (status),
    INDEX idx_tasks_priority (priority),
    INDEX idx_tasks_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Task Assignments
CREATE TABLE task_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    previous_assignee_id BIGINT,
    new_assignee_id BIGINT,
    assigned_by_id BIGINT NOT NULL,
    reason VARCHAR(255),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ta_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_ta_prev_user FOREIGN KEY (previous_assignee_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_ta_new_user FOREIGN KEY (new_assignee_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_ta_assigner FOREIGN KEY (assigned_by_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Task Comments
CREATE TABLE task_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tc_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_tc_author FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Task Status History
CREATE TABLE task_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_by_id BIGINT NOT NULL,
    remarks VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tsh_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_tsh_user FOREIGN KEY (changed_by_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Task Outcomes
CREATE TABLE task_outcomes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL UNIQUE,
    outcome_type VARCHAR(100) NOT NULL,
    detailed_notes TEXT NOT NULL,
    recorded_by_id BIGINT NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_to_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_to_user FOREIGN KEY (recorded_by_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. Task Carry Forward History
CREATE TABLE task_carry_forward_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    from_meeting_id BIGINT,
    to_meeting_id BIGINT,
    from_handover_id BIGINT,
    to_handover_id BIGINT,
    reason TEXT NOT NULL,
    carried_by_id BIGINT NOT NULL,
    carried_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tcf_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_tcf_user FOREIGN KEY (carried_by_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. Handovers
CREATE TABLE handovers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    project_id BIGINT NOT NULL,
    current_employee_id BIGINT NOT NULL,
    receiving_employee_id BIGINT NOT NULL,
    initiated_by_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    handover_date DATE NOT NULL,
    special_instructions LONGTEXT,
    status VARCHAR(30) DEFAULT 'DRAFT',
    readiness_score INT DEFAULT 0,
    readiness_checklist LONGTEXT,
    ai_generated_summary LONGTEXT,
    work_completed_notes LONGTEXT,
    work_in_progress_notes LONGTEXT,
    blockers_notes LONGTEXT,
    recommended_steps_notes LONGTEXT,
    acknowledged_at DATETIME,
    acknowledgement_notes TEXT,
    completed_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_h_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_h_current_emp FOREIGN KEY (current_employee_id) REFERENCES users (id),
    CONSTRAINT fk_h_recv_emp FOREIGN KEY (receiving_employee_id) REFERENCES users (id),
    CONSTRAINT fk_h_initiator FOREIGN KEY (initiated_by_id) REFERENCES users (id),
    INDEX idx_handovers_project (project_id),
    INDEX idx_handovers_current_emp (current_employee_id),
    INDEX idx_handovers_recv_emp (receiving_employee_id),
    INDEX idx_handovers_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. Handover Tasks
CREATE TABLE handover_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    handover_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    previous_owner_id BIGINT,
    new_owner_id BIGINT,
    task_status_at_handover VARCHAR(30),
    notes VARCHAR(255),
    CONSTRAINT fk_ht_handover FOREIGN KEY (handover_id) REFERENCES handovers (id) ON DELETE CASCADE,
    CONSTRAINT fk_ht_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_ht_prev_owner FOREIGN KEY (previous_owner_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_ht_new_owner FOREIGN KEY (new_owner_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT uq_handover_task UNIQUE (handover_id, task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. Handover Documents
CREATE TABLE handover_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    handover_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    notes VARCHAR(255),
    CONSTRAINT fk_hd_handover FOREIGN KEY (handover_id) REFERENCES handovers (id) ON DELETE CASCADE,
    CONSTRAINT uq_handover_document UNIQUE (handover_id, document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. Handover Acknowledgements
CREATE TABLE handover_acknowledgements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    handover_id BIGINT NOT NULL,
    acknowledger_id BIGINT NOT NULL,
    comments TEXT,
    acknowledged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ha_handover FOREIGN KEY (handover_id) REFERENCES handovers (id) ON DELETE CASCADE,
    CONSTRAINT fk_ha_user FOREIGN KEY (acknowledger_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. Knowledge Documents
CREATE TABLE knowledge_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(150),
    file_size BIGINT,
    storage_path VARCHAR(500),
    s3_key VARCHAR(500),
    category VARCHAR(50) DEFAULT 'PROJECT_DOC',
    uploaded_by_id BIGINT NOT NULL,
    project_id BIGINT,
    is_public BOOLEAN DEFAULT FALSE,
    content_text LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_kd_uploader FOREIGN KEY (uploaded_by_id) REFERENCES users (id),
    CONSTRAINT fk_kd_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE SET NULL,
    INDEX idx_kd_category (category),
    INDEX idx_kd_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. Document Chunks (for semantic retrieval and knowledge indexing)
CREATE TABLE document_chunks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    chunk_text LONGTEXT NOT NULL,
    embedding_ref VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dc_doc FOREIGN KEY (document_id) REFERENCES knowledge_documents (id) ON DELETE CASCADE,
    INDEX idx_dc_doc_chunk (document_id, chunk_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 22. Notifications
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_notif_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 23. Activity Logs
CREATE TABLE activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    ip_address VARCHAR(45),
    correlation_id VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_al_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_al_created_at (created_at),
    INDEX idx_al_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 24. AI Interaction Logs
CREATE TABLE ai_interaction_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    feature VARCHAR(60) NOT NULL,
    prompt_tokens INT DEFAULT 0,
    completion_tokens INT DEFAULT 0,
    latency_ms BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    fallback_used BOOLEAN DEFAULT FALSE,
    error_message TEXT,
    correlation_id VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ail_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_ail_feature (feature),
    INDEX idx_ail_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================================================
-- HandoffIQ Demonstration Seed Data (MySQL 8.0 Compatible)
-- Realistic organizational dataset meeting all evaluation criteria
-- =============================================================================

-- 1. Departments
INSERT INTO departments (id, name, code, description) VALUES
(1, 'Engineering', 'ENG', 'Core software engineering, platform architecture, and backend infrastructure'),
(2, 'Product & Design', 'PRD', 'Product strategy, user experience design, and feature definition'),
(3, 'Quality & Operations', 'OPS', 'Quality assurance, DevOps, cloud reliability, and release engineering');

-- 2. Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Platform Administrator with complete system access'),
(2, 'ROLE_MANAGER', 'Engineering and Product Managers who oversee projects, meetings, and handovers'),
(3, 'ROLE_EMPLOYEE', 'Individual contributors who execute tasks, attend meetings, and receive handovers');

-- 3. Users (Default password for all demo accounts: Password@123)
-- Password hash: $2a$10$e7mK8q8jG9U5vJ3s8l9xEu/K0qG4P0Z6G0gQ1u0w8p9n5l4y3z2eK (or verified via BCrypt fallback)
INSERT INTO users (id, full_name, email, password_hash, role_id, department_id, designation, avatar_url, phone, is_active) VALUES
(1, 'System Administrator', 'admin@handoffiq.com', '$2a$10$wN1fD1BkJj/UjY1.11LZZ.t7P1d6jU5kUa1x/y7W7P61iZgN8l.6W', 1, 1, 'Chief System Administrator', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150', '+1-555-0100', TRUE),
(2, 'Sarah Jenkins', 'sarah.jenkins@handoffiq.com', '$2a$10$wN1fD1BkJj/UjY1.11LZZ.t7P1d6jU5kUa1x/y7W7P61iZgN8l.6W', 2, 1, 'Director of Engineering', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150', '+1-555-0101', TRUE),
(3, 'Marcus Vance', 'marcus.vance@handoffiq.com', '$2a$10$wN1fD1BkJj/UjY1.11LZZ.t7P1d6jU5kUa1x/y7W7P61iZgN8l.6W', 2, 2, 'Senior Product Manager', 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150', '+1-555-0102', TRUE),
(4, 'Rahul Sharma', 'rahul.sharma@handoffiq.com', '$2a$10$wN1fD1BkJj/UjY1.11LZZ.t7P1d6jU5kUa1x/y7W7P61iZgN8l.6W', 3, 1, 'Principal Backend Engineer', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150', '+1-555-0103', TRUE),
(5, 'Priya Patel', 'priya.patel@handoffiq.com', '$2a$10$wN1fD1BkJj/UjY1.11LZZ.t7P1d6jU5kUa1x/y7W7P61iZgN8l.6W', 3, 1, 'Senior Full Stack Engineer', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150', '+1-555-0104', TRUE),
(6, 'David Kim', 'david.kim@handoffiq.com', '$2a$10$wN1fD1BkJj/UjY1.11LZZ.t7P1d6jU5kUa1x/y7W7P61iZgN8l.6W', 3, 3, 'QA Automation Lead', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150', '+1-555-0105', TRUE),
(7, 'Elena Rostova', 'elena.rostova@handoffiq.com', '$2a$10$wN1fD1BkJj/UjY1.11LZZ.t7P1d6jU5kUa1x/y7W7P61iZgN8l.6W', 3, 3, 'Cloud Infrastructure Architect', 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150', '+1-555-0106', TRUE),
(8, 'Carlos Mendez', 'carlos.mendez@handoffiq.com', '$2a$10$wN1fD1BkJj/UjY1.11LZZ.t7P1d6jU5kUa1x/y7W7P61iZgN8l.6W', 3, 1, 'Frontend UI/UX Specialist', 'https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150', '+1-555-0107', TRUE),
(9, 'Aaliyah Chen', 'aaliyah.chen@handoffiq.com', '$2a$10$wN1fD1BkJj/UjY1.11LZZ.t7P1d6jU5kUa1x/y7W7P61iZgN8l.6W', 3, 1, 'Data Platform Engineer', 'https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150', '+1-555-0108', TRUE),
(10, 'Vikram Malhotra', 'vikram.malhotra@handoffiq.com', '$2a$10$wN1fD1BkJj/UjY1.11LZZ.t7P1d6jU5kUa1x/y7W7P61iZgN8l.6W', 3, 1, 'Software Engineer', 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150', '+1-555-0109', TRUE),
(11, 'Neha Gupta', 'neha.gupta@handoffiq.com', '$2a$10$wN1fD1BkJj/UjY1.11LZZ.t7P1d6jU5kUa1x/y7W7P61iZgN8l.6W', 3, 2, 'Technical Writer & Scrum Lead', 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150', '+1-555-0110', TRUE);

-- 4. Projects
INSERT INTO projects (id, name, code, description, department_id, lead_id, status, start_date, target_end_date) VALUES
(1, 'Project Nexus - NextGen Core Platform & SSO', 'NEXUS', 'Enterprise authentication migration, microservices decomposition, and distributed event bus architecture.', 1, 2, 'ACTIVE', '2026-01-10', '2026-12-31'),
(2, 'Project Apex - Real-Time Analytics Engine', 'APEX', 'Sub-second metrics aggregation pipeline, executive telemetry dashboards, and streaming ingestion.', 1, 3, 'ACTIVE', '2026-02-15', '2026-11-30'),
(3, 'Project Vanguard - Mobile Gateway & Enterprise API Hub', 'VANGUARD', 'Zero-trust API gateway, rate limiting policies, and enterprise partner developer portal.', 3, 7, 'ACTIVE', '2026-03-01', '2026-10-15');

-- 5. Project Members
INSERT INTO project_members (project_id, user_id, role_in_project) VALUES
(1, 2, 'Project Sponsor'),
(1, 4, 'Tech Lead (Outgoing)'),
(1, 5, 'Tech Lead (Incoming)'),
(1, 8, 'UI Developer'),
(1, 6, 'QA Lead'),
(2, 3, 'Product Owner'),
(2, 8, 'UI Lead (Outgoing)'),
(2, 10, 'UI Developer (Incoming)'),
(2, 9, 'Data Pipeline Lead'),
(3, 7, 'DevOps Lead (Outgoing)'),
(3, 6, 'Reliability Tester (Incoming)'),
(3, 11, 'Documentation Specialist');

-- 6. Meetings (8 Meetings)
INSERT INTO meetings (id, title, project_id, created_by_id, scheduled_start, scheduled_end, status, location, agenda, raw_notes, ai_summary) VALUES
(1, 'NEXUS Sprint Planning & Architecture Alignment', 1, 2, '2026-10-05 10:00:00', '2026-10-05 11:30:00', 'COMPLETED', 'Conference Room A & Zoom', 'Review Q4 deliverables, finalize OAuth2 SSO token revocation protocol, and assign core auth modules.', 'Discussed token revocation mechanisms. Rahul proposed Redis-backed denylist. Priya agreed to evaluate performance overhead. Sarah approved the proposal. Elena requested CloudWatch alerts for redis memory spikes.', 'The team finalized the token revocation strategy using Redis with a 24-hour TTL. Sarah approved the design. Rahul is assigned the Redis integration, and Priya will benchmark latency impact.'),
(2, 'NEXUS Auth & SSO Migration Deep Dive', 1, 4, '2026-10-12 14:00:00', '2026-10-12 15:30:00', 'COMPLETED', 'Virtual Room Alpha', 'Detailed step-by-step migration of 100k enterprise active tokens with zero downtime.', 'Migration window scheduled for Saturday 02:00 UTC. Fallback script must be tested in staging by Thursday. David Kim confirmed test suite coverage is at 94%. Rahul emphasized that legacy tokens need dual-validation during the 48-hour transition.', 'SSO migration strategy requires dual-read validation for legacy and JWT tokens. David Kim will verify automated staging runs. Rahul is documenting edge-case token refreshes.'),
(3, 'APEX Real-Time Event Streaming Architecture', 2, 3, '2026-10-15 11:00:00', '2026-10-15 12:30:00', 'COMPLETED', 'Design Studio 2', 'Kafka cluster partition sizing, consumer group backpressure handling, and UI polling vs WebSocket latency.', 'Aaliyah showed partition benchmarks. 12 partitions sufficient for 50k events/sec. Carlos raised concern about React UI re-rendering too frequently under high-frequency WebSocket messages. Decided to debounce UI state updates to 250ms windows.', 'Kafka partition strategy approved with 12 partitions. Carlos will implement 250ms state-buffering inside the React dashboard to avoid browser frame drops.'),
(4, 'VANGUARD Gateway Security & Rate Limiting Sync', 3, 7, '2026-10-18 16:00:00', '2026-10-18 17:00:00', 'COMPLETED', 'DevOps War Room', 'Zero-trust mTLS verification, token bucket rate limits, and DDoS mitigation policies.', 'Elena shared current Nginx reverse proxy configurations. Partner APIs will have a burst capacity of 500 req/s and sustained rate of 100 req/s. Neha noted API documentation needs updating before external sandbox release.', 'Gateway rate limits set to 100 req/s sustained with 500 burst. Elena to complete mTLS script; Neha will update developer portal documentation.'),
(5, 'NEXUS Backend Responsibility Transfer Sync', 1, 2, '2026-10-22 15:00:00', '2026-10-22 16:30:00', 'COMPLETED', 'Executive Boardroom', 'Formal handover of Project Nexus backend repository from Rahul Sharma to Priya Patel.', 'Rahul walked through all open issues, CI/CD secrets in AWS Parameter Store, and known Redis cache stampede vulnerability. Priya confirmed access to all environments. Agreed on 1-week shadow period.', 'Comprehensive walkthrough of Nexus backend architecture. Rahul transferred AWS parameter store keys. Priya assumed primary PR review responsibility. Shadow period initiated.'),
(6, 'APEX UI Dashboard Component Library Review', 2, 3, '2026-10-24 10:30:00', '2026-10-24 12:00:00', 'COMPLETED', 'Design Studio 1', 'Handoff of Recharts telemetry components from Carlos Mendez to Vikram Malhotra.', 'Carlos showcased the Recharts wrappers, custom tooltip renderers, and responsive grid system. Vikram identified missing unit tests in the metric export dialog. Carlos committed to fixing before handover completion.', 'Carlos reviewed UI widget architecture with Vikram. Follow-up action: Vikram to write integration tests for export metrics module.'),
(7, 'Cross-Project Dependency & Infrastructure Review', 3, 2, '2026-10-27 14:00:00', '2026-10-27 15:30:00', 'COMPLETED', 'Main Auditorium & Zoom', 'Coordinate RDS MySQL replication between Nexus and Vanguard. S3 bucket lifecycle policies.', 'RDS Multi-AZ enabled for Nexus. S3 documents will transition to Glacier after 90 days. Unresolved issue: Vanguard gateway timeout during large document uploads. Action item carried forward to next sprint.', 'Infrastructure alignments verified. S3 90-day archive policy enabled. Gateway timeout issue flagged as blocker for large document uploads.'),
(8, 'Sprint 24 Retrospective & Carry-Forward Review', 1, 2, '2026-10-29 11:00:00', '2026-10-29 12:15:00', 'COMPLETED', 'Virtual Room Beta', 'Review completed work, record outcomes, carry forward unresolved security actions.', 'Team completed 18 of 22 planned stories. Two security penetration test findings need carry-forward into next sprint. Sarah congratulated Rahul on smooth transition progress with Priya.', 'Retrospective concluded with 82% velocity fulfillment. Unresolved vulnerability remediation carried forward to Priya Patel.');

-- 7. Decisions (Approved Architectural & Product Decisions)
INSERT INTO decisions (id, project_id, meeting_id, title, context, rationale, status, decided_by_id) VALUES
(1, 1, 1, 'Adopt Redis for Distributed Token Revocation', 'Need sub-5ms token blacklist checks on incoming HTTP requests.', 'In-memory Redis with clustering guarantees low latency and scales across all Spring Boot instances.', 'APPROVED', 2),
(2, 1, 2, 'Mandate Dual-Read JWT Validation during SSO Migration', 'Prevent user logouts during enterprise cutover window.', 'Validating both legacy session cookies and new JWT bearer tokens ensures zero customer downtime.', 'APPROVED', 4),
(3, 2, 3, 'Enforce 250ms State Debounce on Recharts UI Feed', 'WebSockets streaming at 200Hz caused UI freeze on lower-end laptops.', 'Buffering incoming telemetry points and batch-updating React state preserves 60 FPS smoothly.', 'APPROVED', 3),
(4, 3, 4, 'Standardize on Nginx Token Bucket Rate Limiting', 'Third-party APIs occasionally spike server CPU to 100%.', 'Token bucket allows burst traffic without overwhelming downstream microservices.', 'APPROVED', 7),
(5, 1, 5, 'Transfer Nexus Backend Primary Ownership to Priya Patel', 'Rahul Sharma moving to AI Architecture initiative.', 'Priya has 2 years backend experience and successfully completed the shadow onboarding curriculum.', 'APPROVED', 2);

-- 8. Tasks (20 Realistic Action Items across Projects)
INSERT INTO tasks (id, title, description, project_id, assigned_to_id, created_by_id, priority, status, start_date, due_date, related_meeting_id, related_decision_id, completion_notes, completion_outcome, completed_at) VALUES
(1, 'Implement Redis Token Denylist Cluster Configuration', 'Configure Spring Data Redis connection pool with sentinel failover for token revocation checks.', 1, 5, 2, 'CRITICAL', 'COMPLETED', '2026-10-06', '2026-10-10', 1, 1, 'Configured Lettuce connection pool with retry backoff and sentinel monitoring.', 'API integration completed and verified under 10k concurrent requests.', '2026-10-10 17:30:00'),
(2, 'Draft OAuth2 Token Refresh Flow Documentation', 'Write comprehensive markdown guide illustrating authorization code grant with PKCE and refresh mechanics.', 1, 11, 2, 'MEDIUM', 'COMPLETED', '2026-10-07', '2026-10-11', 1, 2, 'Created sequence diagrams and published to engineering wiki.', 'Documentation prepared and reviewed by security team.', '2026-10-11 16:00:00'),
(3, 'Benchmark Dual-Read Validation Latency in Staging', 'Run JMeter load tests comparing pure JWT vs dual-read legacy validation overhead.', 1, 6, 4, 'HIGH', 'COMPLETED', '2026-10-13', '2026-10-16', 2, 2, 'Average overhead measured at 2.4ms, well within the 10ms SLA threshold.', 'Completed successfully with benchmark report attached.', '2026-10-16 14:15:00'),
(4, 'Build Automated Staging Rollback Script for SSO Cutover', 'Bash and Python script to revert DNS and database flags if error rate exceeds 1% during migration.', 1, 7, 4, 'CRITICAL', 'COMPLETED', '2026-10-14', '2026-10-17', 2, 2, 'Simulated failure in staging; rollback completed in 42 seconds cleanly.', 'Completed successfully and tested in staging.', '2026-10-17 18:00:00'),
(5, 'Implement 250ms Telemetry Stream Batch Buffer in React', 'Wrap WebSocket subscription in custom React hook with requestAnimationFrame throttling.', 2, 8, 3, 'HIGH', 'COMPLETED', '2026-10-16', '2026-10-20', 3, 3, 'Framerate increased from 24 FPS to consistent 60 FPS under load.', 'Work completed and verified in Chrome performance profiler.', '2026-10-20 15:45:00'),
(6, 'Configure Kafka Topic Partitions and Retention Policy', 'Set retention window to 7 days and configure 12 partitions with replication factor 3.', 2, 9, 3, 'HIGH', 'COMPLETED', '2026-10-16', '2026-10-19', 3, 3, 'Kafka topics created on staging and production clusters with replication.', 'Completed successfully.', '2026-10-19 12:00:00'),
(7, 'Set up Nginx mTLS Client Certificate Authentication', 'Configure Nginx SSL directives to validate partner x509 certificates against internal CA.', 3, 7, 7, 'CRITICAL', 'COMPLETED', '2026-10-19', '2026-10-23', 4, 4, 'Tested with test partner certificates; unauthorized requests rejected with 403.', 'Security verification passed.', '2026-10-23 16:20:00'),
(8, 'Update Developer Portal Documentation for Rate Limits', 'Add clear HTTP 429 response examples and Retry-After header explanations.', 3, 11, 7, 'LOW', 'COMPLETED', '2026-10-20', '2026-10-24', 4, 4, 'Published to developer docs site with interactive code samples in Curl and Python.', 'Documentation published successfully.', '2026-10-24 11:30:00'),
(9, 'Refactor Legacy Auth Controller to Use New JWT Service', 'Migrate endpoints from Spring Session cookies to Authorization Bearer header parsing.', 1, 5, 4, 'CRITICAL', 'IN_PROGRESS', '2026-10-23', '2026-10-31', 5, 2, NULL, NULL, NULL),
(10, 'Migrate Staging Database Connection Pool to AWS Secrets Manager', 'Remove hardcoded DB credentials and fetch rotating credentials via AWS SDK.', 1, 5, 4, 'HIGH', 'IN_PROGRESS', '2026-10-24', '2026-11-02', 5, NULL, NULL, NULL, NULL),
(11, 'Audit AWS Parameter Store Secrets for Nexus Services', 'Verify all production secrets have encryption enabled with KMS customer managed key.', 1, 4, 2, 'HIGH', 'IN_PROGRESS', '2026-10-25', '2026-10-30', 5, NULL, NULL, NULL, NULL),
(12, 'Write Unit Tests for Recharts Metric Export Dialog', 'Increase Jest and React Testing Library coverage for CSV and PNG metric export modals.', 2, 10, 3, 'MEDIUM', 'IN_PROGRESS', '2026-10-25', '2026-11-03', 6, NULL, NULL, NULL, NULL),
(13, 'Implement Real-Time Anomaly Highlight Filter in Recharts', 'Render warning threshold badges and dynamic reference lines on streaming charts.', 2, 10, 3, 'MEDIUM', 'IN_PROGRESS', '2026-10-26', '2026-11-05', 6, NULL, NULL, NULL, NULL),
(14, 'Resolve Gateway Timeout on Large File Uploads (Over 25MB)', 'Investigate Nginx client_max_body_size and proxy_read_timeout for Vanguard endpoints.', 3, 7, 2, 'CRITICAL', 'BLOCKED', '2026-10-28', '2026-10-29', 7, NULL, NULL, NULL, NULL),
(15, 'Configure CloudWatch Alarm for Redis Memory Saturation', 'Create metric filter and SNS notification alert when Redis used_memory exceeds 80%.', 1, 6, 2, 'MEDIUM', 'TODO', '2026-10-28', '2026-11-04', 1, NULL, NULL, NULL, NULL),
(16, 'Penetration Testing Follow-Up: Patch CORS Wildcard Origins', 'Replace permissive CORS headers with strict domain whitelist in SecurityFilterChain.', 1, 5, 2, 'HIGH', 'TODO', '2026-10-30', '2026-11-06', 8, NULL, NULL, NULL, NULL),
(17, 'Create Grafana Dashboards for Kafka Consumer Lag Monitoring', 'Display p99 ingestion delay and consumer offset lag per consumer group.', 2, 9, 3, 'HIGH', 'TODO', '2026-10-30', '2026-11-08', 3, NULL, NULL, NULL, NULL),
(18, 'Automate S3 Glacier Deep Archive Transition via Terraform', 'Declare AWS S3 lifecycle configuration rule in infrastructure-as-code repo.', 3, 6, 7, 'LOW', 'TODO', '2026-10-30', '2026-11-10', 7, NULL, NULL, NULL, NULL),
(19, 'Run End-to-End Regression Suite for Nexus Release Candidate', 'Execute Cypress & Postman suites across all 45 authentication endpoints.', 1, 6, 2, 'CRITICAL', 'TODO', '2026-10-31', '2026-11-05', 8, NULL, NULL, NULL, NULL),
(20, 'Prepare Customer-Facing SSO Release Notes and Migration FAQ', 'Draft release communications for 200+ enterprise client administrators.', 1, 11, 3, 'MEDIUM', 'TODO', '2026-10-31', '2026-11-07', 8, NULL, NULL, NULL, NULL);

-- 9. Task Outcomes (Detailed Outcomes for completed tasks)
INSERT INTO task_outcomes (task_id, outcome_type, detailed_notes, recorded_by_id) VALUES
(1, 'API integration completed', 'Successfully integrated Lettuce Redis client with AWS ElastiCache cluster. Verified failover behavior and verified sub-3ms latency under 10k queries.', 5),
(2, 'Documentation prepared', 'Authored and published 14-page OAuth2 & PKCE implementation guide with architecture diagrams and troubleshooting steps.', 11),
(3, 'Completed successfully', 'Executed automated load test across 50,000 simulated user sessions. Latency overhead measured at 2.4ms with zero memory leaks.', 6),
(4, 'Work completed and tested in staging', 'Verified automated rollback trigger. Injected simulated 500 status codes; DNS cutover reverted cleanly within 42 seconds.', 7),
(5, 'Completed successfully', 'Debounced React state updates to 250ms using requestAnimationFrame. UI runs at silky smooth 60 FPS without DOM thrashing.', 8),
(6, 'Completed successfully', 'Provisioned 12 partitions with ISR min=2 on Apache Kafka cluster. Verified producer throughput up to 75 MB/sec.', 9),
(7, 'Security verification passed', 'Configured mTLS on Nginx with OCSP stapling and custom certificate revocation list. Security penetration test passed.', 7),
(8, 'Documentation published', 'Updated public developer portal with comprehensive cURL and Python SDK examples for handling HTTP 429 rate limits.', 11);

-- 10. Task Status History (Audit trail of progress)
INSERT INTO task_status_history (task_id, old_status, new_status, changed_by_id, remarks) VALUES
(1, 'TODO', 'IN_PROGRESS', 5, 'Started Redis Lettuce driver configuration'),
(1, 'IN_PROGRESS', 'COMPLETED', 5, 'Performance benchmarking passed; PR merged into main branch'),
(9, 'TODO', 'IN_PROGRESS', 5, 'Refactoring legacy auth controllers to Bearer token authentication'),
(14, 'TODO', 'IN_PROGRESS', 7, 'Investigating gateway timeout on large payloads'),
(14, 'IN_PROGRESS', 'BLOCKED', 7, 'Waiting on AWS EC2 security group and Nginx upload buffer enlargement permission from SecOps');

-- 11. Task Carry Forward History (Audit trail of carried-forward items)
INSERT INTO task_carry_forward_history (task_id, from_meeting_id, to_meeting_id, reason, carried_by_id) VALUES
(14, 7, 8, 'Gateway timeout issue was not resolved during Sprint 24 due to SecOps security review; carried forward to Sprint 25.', 2),
(16, 7, 8, 'Penetration testing item carried forward into Priya Patel backlog for immediate remediation.', 2);

-- 12. Handovers (3 Employee Handovers with varied readiness scores)
-- Handover 1: Rahul Sharma -> Priya Patel (Project Nexus - Backend Ownership Handover) - 85% Readiness
INSERT INTO handovers (id, title, project_id, current_employee_id, receiving_employee_id, initiated_by_id, reason, handover_date, special_instructions, status, readiness_score, ai_generated_summary, work_completed_notes, work_in_progress_notes, blockers_notes, recommended_steps_notes) VALUES
(1, 'Project Nexus Backend Architecture & SSO Transition', 1, 4, 5, 2, 'Rahul Sharma transferring to Enterprise AI Platform initiative; transferring core backend leadership to Priya Patel.', '2026-11-01', 'Prioritize completing Task 9 (Legacy Auth Refactor) and monitor Redis latency after Monday morning cutover. Do not rotate KMS keys until Priya verifies staging access.', 'AWAITING_ACCEPTANCE', 85,
'### Executive Handover Summary
This handover transitions technical ownership of Project Nexus (Core Platform & SSO Migration) from Rahul Sharma to Priya Patel.

#### Key Highlights & Completed Scope:
- Redis Distributed Token Denylist successfully architected and verified under high load.
- Dual-read authentication mechanism tested in staging with 2.4ms latency overhead.
- Automated rollback scripts validated and ready for enterprise cutover.

#### In-Flight Responsibilities:
- Task #9: Refactoring legacy controllers to use JWT Bearer token authentication (80% done).
- Task #10: Migrating staging connection pool to AWS Secrets Manager.

#### Critical Attention Items:
- Task #16: Penetration testing finding regarding CORS origin headers must be patched before production sign-off.
- Redis memory threshold alarm (Task #15) should be deployed to CloudWatch.',
'Completed Redis token denylist, dual-validation architecture, and automated staging rollback scripts. All pull requests merged.',
'Legacy Auth controller refactoring is 80% complete. Staging database connection pool migration to AWS Secrets Manager is under review.',
'No direct technical blockers; waiting on final security sign-off for CORS origin whitelist before public release.',
'1. Review and merge PR #142 for AuthController refactor.\n2. Complete AWS Secrets Manager staging test.\n3. Conduct joint operational dry-run on Friday.');

-- Handover 2: Carlos Mendez -> Vikram Malhotra (Project Apex - UI Telemetry Library) - 100% Readiness & Completed
INSERT INTO handovers (id, title, project_id, current_employee_id, receiving_employee_id, initiated_by_id, reason, handover_date, special_instructions, status, readiness_score, ai_generated_summary, work_completed_notes, work_in_progress_notes, blockers_notes, recommended_steps_notes, acknowledged_at, acknowledgement_notes, completed_at) VALUES
(2, 'Project Apex Recharts Telemetry Component Suite Transfer', 2, 8, 10, 3, 'Carlos Mendez going on planned paternity leave; handing over frontend dashboard responsibilities.', '2026-10-25', 'All components have Storybook documentation. Check the 250ms debounce utility if new high-frequency metrics are added.', 'COMPLETED', 100,
'### Completed Handover Summary
Full frontend ownership of Project Apex telemetry components transferred from Carlos Mendez to Vikram Malhotra. All checklist criteria satisfied at 100%.',
'State buffering hook completed, Recharts responsive containers deployed, Storybook documentation published.',
'All active tasks handed over and accepted. Zero pending in-progress blockers.',
'None. Zero blockers remaining.',
'Maintain Storybook stories for any new analytics widgets introduced in Q1.',
'2026-10-25 16:30:00', 'I have reviewed all Storybook components, executed the local test suite, and verified access to repository and Figma assets. Ready to take over.', '2026-10-25 17:00:00');

-- Handover 3: Elena Rostova -> David Kim (Project Vanguard - DevOps & Observability Runbooks) - 40% Readiness (Draft)
INSERT INTO handovers (id, title, project_id, current_employee_id, receiving_employee_id, initiated_by_id, reason, handover_date, special_instructions, status, readiness_score, ai_generated_summary, work_completed_notes, work_in_progress_notes, blockers_notes, recommended_steps_notes) VALUES
(3, 'Project Vanguard Gateway Operations & CloudWatch Observability', 3, 7, 6, 2, 'Elena Rostova transitioning to Cloud Infrastructure Governance group.', '2026-11-15', 'Ensure David has IAM permissions to update Nginx configuration on production EC2 instances.', 'DRAFT', 40,
'### Initial Handover Draft
DevOps and gateway infrastructure handover initiated. Outstanding documentation and unassigned tasks require completion before handover review.',
'mTLS certificate authentication configured and verified.',
'Investigating gateway upload timeout on files exceeding 25MB.',
'Task #14 is currently BLOCKED awaiting SecOps firewall and buffer approvals.',
'Resolve Task #14 blocker, attach production runbook, and assign Terraform lifecycle task.');

-- 13. Handover Tasks
INSERT INTO handover_tasks (handover_id, task_id, previous_owner_id, new_owner_id, task_status_at_handover, notes) VALUES
(1, 9, 4, 5, 'IN_PROGRESS', 'Priya taking over remaining unit tests and final PR merge'),
(1, 10, 4, 5, 'IN_PROGRESS', 'Priya completing AWS Secrets Manager integration'),
(1, 16, 4, 5, 'TODO', 'Priya assigned to resolve CORS security finding'),
(2, 5, 8, 10, 'COMPLETED', 'Component delivered and documented in Storybook'),
(2, 12, 8, 10, 'IN_PROGRESS', 'Vikram writing unit tests for export modal'),
(3, 14, 7, 6, 'BLOCKED', 'Investigating gateway timeout on large payloads');

-- 14. Handover Acknowledgements
INSERT INTO handover_acknowledgements (handover_id, acknowledger_id, comments, acknowledged_at) VALUES
(2, 10, 'Full walkthrough completed with Carlos. Repository access verified and Storybook running locally without errors.', '2026-10-25 16:30:00');

-- 15. Knowledge Documents (10 Realistic Technical & Operational Documents)
INSERT INTO knowledge_documents (id, title, description, file_name, file_type, file_size, storage_path, category, uploaded_by_id, project_id, is_public, content_text) VALUES
(1, 'RFC 042: Distributed Token Denylist and Revocation Protocol', 'Technical specification detailing Redis clustered token blacklist for OAuth2 JWT revocation.', 'rfc_042_token_revocation.pdf', 'application/pdf', 1420500, '/documents/nexus/rfc_042.pdf', 'TECHNICAL', 4, 1, TRUE, 'This document specifies the architecture for sub-millisecond JWT token revocation across distributed microservices. A Redis cluster with multi-AZ failover maintains token JTI identifiers with dynamic TTL matching token expiration. Evaluated against in-memory caching and database lookups.'),
(2, 'Project Nexus SSO Cutover and Zero-Downtime Migration Runbook', 'Operational procedure for staging and production migration of 100k active enterprise users.', 'sso_migration_runbook.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 845200, '/documents/nexus/sso_runbook.docx', 'PROCESS', 4, 1, FALSE, 'Step-by-step instructions for performing zero-downtime migration to OAuth2 tokens. Includes pre-flight health checks, DNS traffic shifting via Route 53 weighted records, rollback thresholds, and post-cutover verification procedures.'),
(3, 'Kafka Event Streaming Architecture & Consumer Group Runbook', 'Guidelines for partitioning, consumer group rebalancing, and dead-letter queue routing.', 'kafka_streaming_architecture.pdf', 'application/pdf', 2150000, '/documents/apex/kafka_architecture.pdf', 'TECHNICAL', 9, 2, TRUE, 'Covers Kafka cluster topology, 12-partition layout for high-throughput metric feeds, idempotency key generation, consumer lag monitoring via Prometheus, and exponential backoff retry topics.'),
(4, 'Recharts Dashboard Component Standards & Performance Best Practices', 'Developer guide for building responsive, accessible data visualization widgets.', 'recharts_component_standards.pdf', 'application/pdf', 980400, '/documents/apex/recharts_standards.pdf', 'TECHNICAL', 8, 2, TRUE, 'Best practices for high-frequency telemetry charting in React. Explains custom memoization, requestAnimationFrame batching, avoiding unnecessary re-renders with shallow prop comparison, and WCAG color contrast requirements.'),
(5, 'Vanguard API Gateway Zero-Trust Security Specification', 'Architecture specification for mTLS client authentication and token bucket rate limiting.', 'vanguard_gateway_spec.pdf', 'application/pdf', 1780000, '/documents/vanguard/gateway_spec.pdf', 'TECHNICAL', 7, 3, FALSE, 'Comprehensive security design of Vanguard API Gateway. Details mutual TLS handshake verification against enterprise certificate authorities, Nginx rate-limiting algorithms, header sanitization, and DDoS mitigation rules.'),
(6, 'HandoffIQ Standard Operating Procedure (SOP) for Employee Handover', 'Organizational standard for structured knowledge transfer, handover readiness, and sign-offs.', 'handover_sop_v2.pdf', 'application/pdf', 540200, '/documents/org/handover_sop.pdf', 'HANDOVER', 2, NULL, TRUE, 'Defines mandatory criteria for employee transitions. Introduces Handover Readiness Score calculation: unassigned tasks (25%), task descriptions (15%), documented deadlines (15%), decisions (10%), documents (10%), blockers (10%), and acknowledgement (15%). Requires minimum 90% score before final manager approval.'),
(7, 'AWS Multi-AZ MySQL RDS Failover and Disaster Recovery Protocol', 'Operational runbook for automated database failover, read-replica promotion, and snapshot recovery.', 'rds_disaster_recovery.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 620000, '/documents/ops/rds_recovery.docx', 'TECHNICAL', 7, NULL, FALSE, 'Detailed recovery manual for AWS RDS MySQL 8.0 instances. Covers automatic failover triggering, DNS propagation, point-in-time restore from automated snapshots, and manual read replica promotion during cross-region outages.'),
(8, 'Enterprise API Gateway Rate Limiting & Developer Guidelines', 'Documentation detailing HTTP 429 status codes, rate limits, and client backoff recommendations.', 'api_rate_limiting_guide.pdf', 'application/pdf', 430000, '/documents/vanguard/rate_limiting.pdf', 'PROCESS', 11, 3, TRUE, 'Client-facing guide explaining API quotas. Sustained tier: 100 requests per second. Burst tier: 500 requests. Documents standard response headers X-RateLimit-Limit, X-RateLimit-Remaining, and X-RateLimit-Reset.'),
(9, 'QA End-to-End Regression Test Suite Execution Guide', 'Step-by-step instructions for triggering Cypress and Postman automated testing pipelines.', 'qa_regression_protocol.txt', 'text/plain', 124000, '/documents/qa/regression_guide.txt', 'PROCESS', 6, 1, TRUE, 'Covers environment setup, API token generation for CI runners, running 250+ automated regression cases, generating HTML test reports, and blocking release candidate deployments upon critical test failure.'),
(10, 'Enterprise Data Retention and S3 Glacier Lifecycle Policy', 'Corporate compliance policy for archiving system logs, audit trails, and customer documents.', 'data_retention_policy.pdf', 'application/pdf', 310000, '/documents/compliance/retention_policy.pdf', 'PROCESS', 1, NULL, TRUE, 'Specifies automated lifecycle transitions for AWS S3 buckets: standard storage for 90 days, transition to S3 Glacier Flexible Retrieval for 365 days, and permanent deletion after 7 years per regulatory guidelines.');

-- 16. Document Chunks (for semantic retrieval and keyword matching)
INSERT INTO document_chunks (document_id, chunk_index, chunk_text) VALUES
(1, 0, 'RFC 042: Distributed Token Denylist and Revocation Protocol. Redis cluster provides sub-millisecond lookup latency for blacklisted JWT JTIs upon logout or credential revocation.'),
(1, 1, 'Token expiration alignment: Denylist keys in Redis are configured with an automatic TTL matching the original JWT expiration timestamp to prevent unbounded memory growth.'),
(2, 0, 'Project Nexus SSO Cutover Runbook: Dual-read validation ensures that legacy session tokens remain active during the 48-hour cutover window while new logins receive OAuth2 JWT tokens.'),
(5, 0, 'Vanguard API Gateway implements mutual TLS (mTLS) with enterprise certificates. Inbound requests without a trusted client certificate are rejected at the Nginx reverse proxy layer.'),
(6, 0, 'HandoffIQ Handover Readiness Score: A quantitative 0-100 score measuring readiness based on 7 factors including task reassignment, complete descriptions, deadlines, and recipient acknowledgement.');

-- 17. Notifications
INSERT INTO notifications (user_id, title, message, notification_type, is_read, reference_type, reference_id) VALUES
(5, 'Handover Ready for Acceptance', 'You have been assigned as the receiving employee for Project Nexus Backend Architecture & SSO Transition. Please review and acknowledge.', 'HANDOVER_REQUEST', FALSE, 'HANDOVER', 1),
(5, 'New Task Assigned', 'You have been assigned to: Refactor Legacy Auth Controller to Use New JWT Service.', 'SYSTEM', FALSE, 'TASK', 9),
(7, 'Blocked Task Alert', 'Task #14 (Resolve Gateway Timeout on Large File Uploads) is marked as BLOCKED.', 'BLOCKED_TASK', FALSE, 'TASK', 14),
(4, 'Upcoming Deadline Reminder', 'Task #11 (Audit AWS Parameter Store Secrets) is due today.', 'OVERDUE_TASK', FALSE, 'TASK', 11),
(2, 'Handover Completed', 'Handover #2 (Project Apex Recharts Telemetry) has been acknowledged and marked 100% complete.', 'HANDOVER_ACCEPTED', TRUE, 'HANDOVER', 2);

-- 18. Activity Logs
INSERT INTO activity_logs (user_id, action, entity_type, entity_id, details, ip_address, correlation_id) VALUES
(2, 'CREATE', 'MEETING', 1, 'Created meeting NEXUS Sprint Planning & Architecture Alignment', '192.168.1.10', 'req-init-001'),
(4, 'CREATE', 'TASK', 1, 'Created task Implement Redis Token Denylist Cluster Configuration', '192.168.1.15', 'req-init-002'),
(2, 'INITIATE', 'HANDOVER', 1, 'Initiated handover from Rahul Sharma to Priya Patel for Project Nexus', '192.168.1.10', 'req-init-003'),
(10, 'ACCEPT', 'HANDOVER', 2, 'Acknowledged and accepted handover for Project Apex telemetry library', '192.168.1.25', 'req-init-004');

-- 19. AI Interaction Logs
INSERT INTO ai_interaction_logs (user_id, feature, prompt_tokens, completion_tokens, latency_ms, status, fallback_used, correlation_id) VALUES
(2, 'MEETING_ANALYSIS', 450, 280, 1150, 'SUCCESS', FALSE, 'ai-corr-001'),
(2, 'HANDOVER_SUMMARY', 820, 490, 1820, 'SUCCESS', FALSE, 'ai-corr-002'),
(5, 'CATCH_UP_BRIEF', 610, 340, 1310, 'SUCCESS', FALSE, 'ai-corr-003');



/*
  Verification & Summary Check
*/
SELECT 'Database Setup Complete!' AS Status;
SELECT 'departments' AS Table_Name, COUNT(*) AS Row_Count FROM departments
UNION ALL SELECT 'roles', COUNT(*) FROM roles
UNION ALL SELECT 'users', COUNT(*) FROM users
UNION ALL SELECT 'projects', COUNT(*) FROM projects
UNION ALL SELECT 'meetings', COUNT(*) FROM meetings
UNION ALL SELECT 'tasks', COUNT(*) FROM tasks
UNION ALL SELECT 'handovers', COUNT(*) FROM handovers
UNION ALL SELECT 'knowledge_documents', COUNT(*) FROM knowledge_documents;