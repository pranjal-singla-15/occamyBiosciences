package com.occamy.occamyBiosciences.controller;

import com.occamy.occamyBiosciences.entity.*;
import com.occamy.occamyBiosciences.enums.Role;
import com.occamy.occamyBiosciences.service.FieldOfficerService;
import com.occamy.occamyBiosciences.repository.TaskRepository;
import com.occamy.occamyBiosciences.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/field-officer")
@Slf4j
public class FieldOfficerController {

    @Autowired
    private FieldOfficerService fieldOfficerService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    // Create a new meeting
    @PostMapping("/meetings")
    public ResponseEntity<?> createMeeting(@RequestBody Meeting meeting) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String officerName = authentication.getName();
            User user = fieldOfficerService.findByUserName(officerName);

            if(user != null && user.getRole() == Role.USER) {
                Meeting createdMeeting = fieldOfficerService.createMeeting(meeting);
                log.info("Meeting created with ID: {}", createdMeeting.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(createdMeeting);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

        } catch (RuntimeException e) {
            log.error("Error creating meeting: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Get all meetings for a specific field officer
    @GetMapping("/meetings")
    public ResponseEntity<?> getMyMeetings(@RequestParam Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String officerName = authentication.getName();
            User user = fieldOfficerService.findByUserName(officerName);
            if(user != null && user.getRole() == Role.USER) {
                List<Meeting> meetings = fieldOfficerService.getMyMeetings(userId);
                log.info("Retrieved {} meetings for user {}", meetings.size(), userId);
                return ResponseEntity.ok(meetings);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (RuntimeException e) {
            log.error("Error retrieving meetings: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Start attendance tracking
    @PostMapping("/attendance/start")
    public ResponseEntity<?> startAttendance(
            @RequestParam Long userId,
            @RequestParam Double lat,
            @RequestParam Double lng) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String officerName = authentication.getName();
            User user = fieldOfficerService.findByUserName(officerName);
            if(user != null && user.getRole() == Role.USER) {
                fieldOfficerService.startAttendance(userId, lat, lng);
                log.info("Attendance started for user {} at location ({}, {})", userId, lat, lng);
                return ResponseEntity.ok(Map.of("message", "Attendance started successfully"));
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (RuntimeException e) {
            log.error("Error starting attendance: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // End attendance tracking
    @PostMapping("/attendance/end")
    public ResponseEntity<?> endAttendance(
            @RequestParam Long userId,
            @RequestParam Double lat,
            @RequestParam Double lng) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String officerName = authentication.getName();
            User user = fieldOfficerService.findByUserName(officerName);
            if(user != null && user.getRole() == Role.USER){
                fieldOfficerService.endAttendance(userId, lat, lng);
                log.info("Attendance ended for user {} at location ({}, {})", userId, lat, lng);
                return ResponseEntity.ok(Map.of("message", "Attendance ended successfully"));
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

        } catch (RuntimeException e) {
            log.error("Error ending attendance: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Add a product review
    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<?> addProductReview(
            @PathVariable Long productId,
            @RequestParam Long userId,
            @RequestParam String review) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String officerName = authentication.getName();
            User user = fieldOfficerService.findByUserName(officerName);
            if(user != null && user.getRole() == Role.USER){
                fieldOfficerService.addProductReview(userId, productId, review);
                log.info("User {} added review for product {}", userId, productId);
                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Review added successfully"));
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

        } catch (RuntimeException e) {
            log.error("Error adding review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Add a photo to a meeting
    @PostMapping("/meetings/{meetingId}/photos")
    public ResponseEntity<?> addMeetingPhoto(
            @PathVariable Long meetingId,
            @RequestParam Long userId,
            @RequestParam String photoUrl) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String officerName = authentication.getName();
            User user = fieldOfficerService.findByUserName(officerName);
            if(user != null && user.getRole() == Role.USER){
                MeetingPhoto meetingPhoto = fieldOfficerService.addMeetingPhoto(meetingId, userId, photoUrl);
                log.info("User {} added photo to meeting {}", userId, meetingId);
                return ResponseEntity.status(HttpStatus.CREATED).body(meetingPhoto);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (RuntimeException e) {
            log.error("Error adding meeting photo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Add product sales and samples (legacy method)
    @PostMapping("/products/{productId}/sales-samples")
    public ResponseEntity<?> addProductSalesAndSamples(
            @PathVariable Long productId,
            @RequestParam int salesCount,
            @RequestParam int samplesCount) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String officerName = authentication.getName();
            User user = fieldOfficerService.findByUserName(officerName);
            if(user != null && user.getRole() == Role.USER) {
                Product product = fieldOfficerService.addProductSalesAndSamples(productId, salesCount, samplesCount);
                log.info("Added {} sales and {} samples for product {}", salesCount, samplesCount, productId);
                return ResponseEntity.ok(product);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (RuntimeException e) {
            log.error("Error adding sales and samples: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Get all products
    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String officerName = authentication.getName();
            User user = fieldOfficerService.findByUserName(officerName);
            if(user != null && user.getRole() == Role.USER) {
                List<Product> products = fieldOfficerService.getAllProducts();
                log.info("Retrieved {} products", products.size());
                return ResponseEntity.ok(products);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (RuntimeException e) {
            log.error("Error retrieving products: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Record product sales and samples for a specific field officer
    @PostMapping("/products/{productId}/record-sales")
    public ResponseEntity<?> recordProductSalesAndSamples(
            @PathVariable Long productId,
            @RequestParam Long officerId,
            @RequestParam int salesCount,
            @RequestParam int samplesCount) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String officerName = authentication.getName();
            User user = fieldOfficerService.findByUserName(officerName);
            if(user != null && user.getRole() == Role.USER) {
                ProductSalesRecord record = fieldOfficerService.recordProductSalesAndSamples(
                        officerId, productId, salesCount, samplesCount);
                log.info("Officer {} recorded {} sales and {} samples for product {}",
                        officerId, salesCount, samplesCount, productId);
                return ResponseEntity.status(HttpStatus.CREATED).body(record);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

        } catch (RuntimeException e) {
            log.error("Error recording sales and samples: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Get all tasks assigned to a field officer
    @GetMapping("/tasks")
    public ResponseEntity<?> getMyTasks(@RequestParam Long officerId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String officerName = authentication.getName();
            User user = fieldOfficerService.findByUserName(officerName);
            if(user != null && user.getRole() == Role.USER) {
                List<com.occamy.occamyBiosciences.dto.TaskDTO> tasks = fieldOfficerService.getTasksForOfficer(officerId);
                log.info("Retrieved {} tasks for officer {}", tasks.size(), officerId);
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

    // Get tasks by status for a field officer
    @GetMapping("/tasks/status/{status}")
    public ResponseEntity<?> getTasksByStatus(
            @RequestParam Long officerId,
            @PathVariable String status) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String officerName = authentication.getName();
            User user = fieldOfficerService.findByUserName(officerName);
            if(user != null && user.getRole() == Role.USER) {
                List<com.occamy.occamyBiosciences.dto.TaskDTO> tasks = fieldOfficerService.getTasksByStatusForOfficer(officerId, status);
                log.info("Retrieved {} tasks with status '{}' for officer {}", tasks.size(), status, officerId);
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

    // Update task status (e.g., from PENDING to COMPLETED)
    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam Long officerId,
            @RequestParam String status) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String officerName = authentication.getName();
            User user = fieldOfficerService.findByUserName(officerName);
            if(user != null && user.getRole() == Role.USER) {
                com.occamy.occamyBiosciences.dto.TaskDTO updatedTask = fieldOfficerService.updateTaskStatus(taskId, officerId, status);
                log.info("Officer {} updated task {} to status '{}'", officerId, taskId, status);
                return ResponseEntity.ok(updatedTask);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (RuntimeException e) {
            log.error("Error updating task status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
