package com.handoffiq.repository;

import com.handoffiq.entity.HandoverTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HandoverTaskRepository extends JpaRepository<HandoverTask, Long> {
    List<HandoverTask> findByHandoverId(Long handoverId);
}
