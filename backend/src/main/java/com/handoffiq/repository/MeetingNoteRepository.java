package com.handoffiq.repository;

import com.handoffiq.entity.MeetingNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingNoteRepository extends JpaRepository<MeetingNote, Long> {
    List<MeetingNote> findByMeetingIdOrderByCreatedAtAsc(Long meetingId);
}
