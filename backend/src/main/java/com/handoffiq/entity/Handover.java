package com.handoffiq.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "handovers")
public class Handover {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_employee_id", nullable = false)
    private User currentEmployee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiving_employee_id", nullable = false)
    private User receivingEmployee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "initiated_by_id", nullable = false)
    private User initiatedBy;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "handover_date", nullable = false)
    private LocalDate handoverDate;

    @Column(name = "special_instructions", columnDefinition = "LONGTEXT")
    private String specialInstructions;

    @Column(length = 30)
    private String status = "DRAFT"; // DRAFT, AWAITING_REVIEW, AWAITING_ACCEPTANCE, ACCEPTED, COMPLETED

    @Column(name = "readiness_score")
    private Integer readinessScore = 0;

    @Column(name = "readiness_checklist", columnDefinition = "LONGTEXT")
    private String readinessChecklist;

    @Column(name = "ai_generated_summary", columnDefinition = "LONGTEXT")
    private String aiGeneratedSummary;

    @Column(name = "work_completed_notes", columnDefinition = "LONGTEXT")
    private String workCompletedNotes;

    @Column(name = "work_in_progress_notes", columnDefinition = "LONGTEXT")
    private String workInProgressNotes;

    @Column(name = "blockers_notes", columnDefinition = "LONGTEXT")
    private String blockersNotes;

    @Column(name = "recommended_steps_notes", columnDefinition = "LONGTEXT")
    private String recommendedStepsNotes;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "acknowledgement_notes", columnDefinition = "TEXT")
    private String acknowledgementNotes;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Handover() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public User getCurrentEmployee() { return currentEmployee; }
    public void setCurrentEmployee(User currentEmployee) { this.currentEmployee = currentEmployee; }

    public User getReceivingEmployee() { return receivingEmployee; }
    public void setReceivingEmployee(User receivingEmployee) { this.receivingEmployee = receivingEmployee; }

    public User getInitiatedBy() { return initiatedBy; }
    public void setInitiatedBy(User initiatedBy) { this.initiatedBy = initiatedBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDate getHandoverDate() { return handoverDate; }
    public void setHandoverDate(LocalDate handoverDate) { this.handoverDate = handoverDate; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getReadinessScore() { return readinessScore; }
    public void setReadinessScore(Integer readinessScore) { this.readinessScore = readinessScore; }

    public String getReadinessChecklist() { return readinessChecklist; }
    public void setReadinessChecklist(String readinessChecklist) { this.readinessChecklist = readinessChecklist; }

    public String getAiGeneratedSummary() { return aiGeneratedSummary; }
    public void setAiGeneratedSummary(String aiGeneratedSummary) { this.aiGeneratedSummary = aiGeneratedSummary; }

    public String getWorkCompletedNotes() { return workCompletedNotes; }
    public void setWorkCompletedNotes(String workCompletedNotes) { this.workCompletedNotes = workCompletedNotes; }

    public String getWorkInProgressNotes() { return workInProgressNotes; }
    public void setWorkInProgressNotes(String workInProgressNotes) { this.workInProgressNotes = workInProgressNotes; }

    public String getBlockersNotes() { return blockersNotes; }
    public void setBlockersNotes(String blockersNotes) { this.blockersNotes = blockersNotes; }

    public String getRecommendedStepsNotes() { return recommendedStepsNotes; }
    public void setRecommendedStepsNotes(String recommendedStepsNotes) { this.recommendedStepsNotes = recommendedStepsNotes; }

    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

    public String getAcknowledgementNotes() { return acknowledgementNotes; }
    public void setAcknowledgementNotes(String acknowledgementNotes) { this.acknowledgementNotes = acknowledgementNotes; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
