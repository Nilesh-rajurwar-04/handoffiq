package com.handoffiq.repository;

import com.handoffiq.entity.Handover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HandoverRepository extends JpaRepository<Handover, Long> {
    List<Handover> findByProjectId(Long projectId);
    List<Handover> findByCurrentEmployeeId(Long userId);
    List<Handover> findByReceivingEmployeeId(Long userId);
    List<Handover> findByStatus(String status);
    long countByStatus(String status);

    @Query("SELECT h FROM Handover h WHERE h.currentEmployee.id = :userId OR h.receivingEmployee.id = :userId ORDER BY h.createdAt DESC")
    List<Handover> findByParticipantUserId(@Param("userId") Long userId);

    List<Handover> findAllByOrderByCreatedAtDesc();
}
