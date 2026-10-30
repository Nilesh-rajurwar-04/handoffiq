package com.handoffiq.repository;

import com.handoffiq.entity.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {
    List<MeetingParticipant> findByMeetingId(Long meetingId);
    List<MeetingParticipant> findByUserId(Long userId);
    Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);
}
