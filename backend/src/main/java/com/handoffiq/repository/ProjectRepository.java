package com.handoffiq.repository;

import com.handoffiq.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByCode(String code);
    List<Project> findByStatus(String status);
    long countByStatus(String status);
    List<Project> findByDepartmentId(Long departmentId);
}
