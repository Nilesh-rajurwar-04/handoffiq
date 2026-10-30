package com.handoffiq.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "meeting_participants", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"meeting_id", "user_id"})
})
public class MeetingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "attendance_status", length = 30)
    private String attendanceStatus = "INVITED"; // INVITED, ATTENDED, ABSENT

    @Column(length = 255)
    private String notes;

    public MeetingParticipant() {}

    public MeetingParticipant(Meeting meeting, User user, String attendanceStatus) {
        this.meeting = meeting;
        this.user = user;
        this.attendanceStatus = attendanceStatus;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Meeting getMeeting() { return meeting; }
    public void setMeeting(Meeting meeting) { this.meeting = meeting; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
