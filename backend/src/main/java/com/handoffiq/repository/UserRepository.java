package com.handoffiq.repository;

import com.handoffiq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByIsActiveTrue();
    long countByIsActiveTrue();
    List<User> findByDepartmentId(Long departmentId);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchUsers(@Param("keyword") String keyword);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:identifier) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT(:identifier, '@%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :identifier, '%')) " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT(:identifier, '%')) " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :identifier, '%')) " +
           "ORDER BY CASE " +
           "  WHEN LOWER(u.email) = LOWER(:identifier) THEN 1 " +
           "  WHEN LOWER(u.email) LIKE LOWER(CONCAT(:identifier, '@%')) THEN 2 " +
           "  WHEN LOWER(u.fullName) LIKE LOWER(CONCAT(:identifier, '%')) THEN 3 " +
           "  ELSE 4 END ASC")
    List<User> findByIdentifier(@Param("identifier") String identifier);
}
