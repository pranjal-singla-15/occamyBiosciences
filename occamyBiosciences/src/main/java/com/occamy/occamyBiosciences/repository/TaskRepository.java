package com.occamy.occamyBiosciences.repository;

import com.occamy.occamyBiosciences.entity.Task;
import com.occamy.occamyBiosciences.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedToOfficer(User officer);

    @Query("SELECT t FROM Task t WHERE t.assignedToOfficer.id = :officerId")
    List<Task> findByAssignedToOfficerId(@Param("officerId") Long officerId);

    List<Task> findByAssignedByAdmin(User admin);

    @Query("SELECT t FROM Task t WHERE t.assignedByAdmin.id = :adminId")
    List<Task> findByAssignedByAdminId(@Param("adminId") Long adminId);

    List<Task> findByAssignedToOfficerAndStatus(User officer, String status);

    @Query("SELECT t FROM Task t WHERE t.assignedToOfficer.id = :officerId AND t.status = :status")
    List<Task> findByAssignedToOfficerIdAndStatus(@Param("officerId") Long officerId, @Param("status") String status);
}
