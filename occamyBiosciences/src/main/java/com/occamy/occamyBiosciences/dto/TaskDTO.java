package com.occamy.occamyBiosciences.dto;

import com.occamy.occamyBiosciences.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private Long assignedToOfficerId;
    private String assignedToOfficerName;
    private Long assignedByAdminId;
    private String assignedByAdminName;
    private LocalDateTime assignedDate;
    private LocalDateTime dueDate;
    private String status;
    private LocalDateTime completedDate;
    private String notes;

    // Constructor to convert Task entity to DTO
    public TaskDTO(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.assignedToOfficerId = task.getAssignedToOfficer().getId();
        this.assignedToOfficerName = task.getAssignedToOfficer().getUserName();
        this.assignedByAdminId = task.getAssignedByAdmin().getId();
        this.assignedByAdminName = task.getAssignedByAdmin().getUserName();
        this.assignedDate = task.getAssignedDate();
        this.dueDate = task.getDueDate();
        this.status = task.getStatus();
        this.completedDate = task.getCompletedDate();
        this.notes = task.getNotes();
    }

    // Static method to convert Task to DTO
    public static TaskDTO fromEntity(Task task) {
        return new TaskDTO(task);
    }
}

