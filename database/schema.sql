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
    file_type VARCHAR(50),
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
