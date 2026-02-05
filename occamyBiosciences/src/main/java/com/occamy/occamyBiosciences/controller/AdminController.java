package com.occamy.occamyBiosciences.controller;

import com.occamy.occamyBiosciences.dto.OfficerMeetingsSalesDTO;
import com.occamy.occamyBiosciences.dto.ProductSalesVsSamplesDTO;
import com.occamy.occamyBiosciences.entity.Meeting;
import com.occamy.occamyBiosciences.entity.Task;
import com.occamy.occamyBiosciences.entity.User;
import com.occamy.occamyBiosciences.enums.MeetingType;
import com.occamy.occamyBiosciences.enums.Role;
import com.occamy.occamyBiosciences.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Create a new field officer
    @PostMapping("/create-field-officers")
    public ResponseEntity<?> createFieldOfficer(
            @RequestParam Long adminId,
            @RequestParam String userName,
            @RequestParam String password,
            @RequestParam String phoneNumber) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminName = authentication.getName();

            User user = adminService.findByUserName(adminName);

            if(user != null && user.getRole() == Role.ADMIN) {
                User fieldOfficer = adminService.createFieldOfficer(adminId, userName, password, phoneNumber);
                log.info("Admin {} created field officer: {}", adminId, userName);
                return ResponseEntity.status(HttpStatus.CREATED).body(fieldOfficer);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (RuntimeException e) {
            log.error("Error creating field officer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Get all field officers managed by an admin
    @GetMapping("/field-officers")
    public ResponseEntity<?> getMyFieldOfficers(@RequestParam Long adminId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminName = authentication.getName();

            User user = adminService.findByUserName(adminName);
            if(user != null && user.getRole() == Role.ADMIN) {
                List<User> fieldOfficers = adminService.getMyFieldOfficers(adminId);
                log.info("Admin {} retrieved {} field officers", adminId, fieldOfficers.size());
                return ResponseEntity.ok(fieldOfficers);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (RuntimeException e) {
            log.error("Error retrieving field officers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Get sales vs samples report for a specific field officer
    @GetMapping("/reports/sales-vs-samples/officer/{officerId}")
    public ResponseEntity<?> getSalesVsSamplesForOfficer(@PathVariable Long officerId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminName = authentication.getName();

            User user = adminService.findByUserName(adminName);
            if(user != null && user.getRole() == Role.ADMIN) {
                List<ProductSalesVsSamplesDTO> report = adminService.getSalesVsSamplesForOfficer(officerId);
                log.info("Retrieved sales vs samples report for officer {}", officerId);
                return ResponseEntity.ok(report);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

        } catch (RuntimeException e) {
            log.error("Error retrieving report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Get sales vs samples report for all field officers
    @GetMapping("/reports/sales-vs-samples/all")
    public ResponseEntity<?> getSalesVsSamplesForAllOfficers() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminName = authentication.getName();
            User user = adminService.findByUserName(adminName);
            if(user != null && user.getRole() == Role.ADMIN) {
                List<ProductSalesVsSamplesDTO> report = adminService.getSalesVsSamplesForAllOfficers();
                log.info("Retrieved sales vs samples report for all officers");
                return ResponseEntity.ok(report);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (RuntimeException e) {
            log.error("Error retrieving report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // Get sales vs samples report for a specific product
    @GetMapping("/reports/sales-vs-samples/product/{productId}")
    public ResponseEntity<?> getSalesVsSamplesForProduct(@PathVariable Long productId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminName = authentication.getName();
            User user = adminService.findByUserName(adminName);
            if(user != null && user.getRole() == Role.ADMIN){
                List<ProductSalesVsSamplesDTO> report = adminService.getSalesVsSamplesForProduct(productId);
                log.info("Retrieved sales vs samples report for product {}", productId);
                return ResponseEntity.ok(report);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

        } catch (RuntimeException e) {
            log.error("Error retrieving report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Assign a task to a field officer
    @PostMapping("/tasks/assign")
    public ResponseEntity<?> assignTaskToFieldOfficer(
            @RequestParam Long adminId,
            @RequestParam Long fieldOfficerId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) String dueDate) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminName = authentication.getName();
            User user = adminService.findByUserName(adminName);
            if(user != null && user.getRole() == Role.ADMIN) {
                LocalDateTime dueDateParsed = dueDate != null ? LocalDateTime.parse(dueDate) : null;
                Task task = adminService.assignTaskToFieldOfficer(adminId, fieldOfficerId, title, description, dueDateParsed);
                log.info("Admin {} assigned task '{}' to field officer {}", adminId, title, fieldOfficerId);
                return ResponseEntity.status(HttpStatus.CREATED).body(task);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

        } catch (RuntimeException e) {
            log.error("Error assigning task: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Get all tasks assigned by an admin
    @GetMapping("/tasks")
    public ResponseEntity<?> getTasksAssignedByAdmin(@RequestParam Long adminId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminName = authentication.getName();
            User user = adminService.findByUserName(adminName);
            if(user != null && user.getRole() == Role.ADMIN){
                List<com.occamy.occamyBiosciences.dto.TaskDTO> tasks = adminService.getTasksAssignedByAdmin(adminId);
                log.info("Admin {} retrieved {} tasks", adminId, tasks.size());
                return ResponseEntity.ok(tasks);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (RuntimeException e) {
            log.error("Error retrieving tasks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Get all tasks assigned to a specific field officer
    @GetMapping("/tasks/officer/{fieldOfficerId}")
    public ResponseEntity<?> getTasksForFieldOfficer(
            @RequestParam Long adminId,
            @PathVariable Long fieldOfficerId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminName = authentication.getName();
            User user = adminService.findByUserName(adminName);
            if(user != null && user.getRole() == Role.ADMIN){
                List<com.occamy.occamyBiosciences.dto.TaskDTO> tasks = adminService.getTasksForFieldOfficer(adminId, fieldOfficerId);
                log.info("Admin {} retrieved {} tasks for field officer {}", adminId, tasks.size(), fieldOfficerId);
                return ResponseEntity.ok(tasks);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (RuntimeException e) {
            log.error("Error retrieving tasks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Get meetings vs sales data for chart
    @GetMapping("/chart/meetings-vs-sales")
    public ResponseEntity<?> getMeetingsVsSalesData(@RequestParam Long adminId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminName = authentication.getName();
            User user = adminService.findByUserName(adminName);
            if(user != null && user.getRole() == Role.ADMIN){
                List<OfficerMeetingsSalesDTO> data = adminService.getMeetingsVsSalesData(adminId);
                log.info("Admin {} retrieved meetings vs sales data for {} officers", adminId, data.size());
                return ResponseEntity.ok(data);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (RuntimeException e) {
            log.error("Error retrieving meetings vs sales data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Assign a meeting to a field officer
    @PostMapping("/meetings/assign")
    public ResponseEntity<?> assignMeetingToFieldOfficer(
            @RequestParam Long adminId,
            @RequestParam Long fieldOfficerId,
            @RequestParam String meetingType,
            @RequestParam String location,
            @RequestParam(required = false) String latitude,
            @RequestParam(required = false) String longitude,
            @RequestParam(required = false) String notes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminName = authentication.getName();
            User user = adminService.findByUserName(adminName);
            if(user != null && user.getRole() == Role.ADMIN) {
                MeetingType type = MeetingType.valueOf(meetingType.toUpperCase());
                Meeting meeting = adminService.assignMeetingToFieldOfficer(
                    adminId, fieldOfficerId, type, location, latitude, longitude, notes
                );
                log.info("Admin {} assigned meeting to field officer {}", adminId, fieldOfficerId);
                return ResponseEntity.status(HttpStatus.CREATED).body(meeting);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

        } catch (RuntimeException e) {
            log.error("Error assigning meeting: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
