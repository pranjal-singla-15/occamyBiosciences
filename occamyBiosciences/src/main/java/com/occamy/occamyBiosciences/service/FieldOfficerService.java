package com.occamy.occamyBiosciences.service;

import com.occamy.occamyBiosciences.dto.TaskDTO;
import com.occamy.occamyBiosciences.entity.*;
import com.occamy.occamyBiosciences.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FieldOfficerService {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendenceRepository attendanceRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private MeetingPhotoRepository meetingPhotoRepository;

    @Autowired
    private ProductSalesRecordRepository productSalesRecordRepository;

    @Autowired
    private TaskRepository taskRepository;

    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + userName));
    }

    public Meeting createMeeting(Meeting meeting){
        meetingRepository.save(meeting);
        return meeting;
    }

    public List<Meeting> getMyMeetings(Long userId){
        List<Meeting> meetings = meetingRepository.findByOfficerId(userId);
        return meetings;
    }

    public void startAttendance(Long userId, Double lat, Double lng){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Attendance> lastAttendance =
                attendanceRepository.findTopByUserOrderByStartTimeDesc(user);

        if (lastAttendance.isPresent() && lastAttendance.get().getEndTime() == null) {
            throw new RuntimeException("Attendance already started.");
        }

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setStartTime(LocalDateTime.now());
        attendance.setStartLat(lat);
        attendance.setStartLng(lng);
        attendanceRepository.save(attendance);
    }


    public void endAttendance(Long userId, double lat, double lng){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Attendance attendance =
                attendanceRepository.findTopByUserOrderByStartTimeDesc(user)
                        .orElseThrow(() -> new RuntimeException("No attendance found to end"));

        if (attendance.getEndTime() != null) {
            throw new RuntimeException("Attendance already ended");
        }

        attendance.setEndTime(LocalDateTime.now());
        attendance.setEndLat(lat);
        attendance.setEndLng(lng);

        attendanceRepository.save(attendance);
    }

    public void addProductReview(Long userId, Long productId, String review){
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductReview productReview = new ProductReview();
        productReview.setUserId(userId);
        productReview.setProduct(product);
        productReview.setReview(review);

        productReviewRepository.save(productReview);
    }

    public MeetingPhoto addMeetingPhoto(Long meetingId, Long userId, String photoUrl){
        // Validate user exists and belongs to the meeting
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        // Verify that the user is the officer who created the meeting
        if (!meeting.getOfficer().getId().equals(userId)) {
            throw new RuntimeException("User is not authorized to add photos to this meeting");
        }

        MeetingPhoto meetingPhoto = new MeetingPhoto();
        meetingPhoto.setPhotoUrl(photoUrl);
        meetingPhoto.setMeeting(meeting);

        return meetingPhotoRepository.save(meetingPhoto);
    }

    public Product addProductSalesAndSamples(Long productId, int salesCount, int samplesCount){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Add the sales and samples to existing values
        product.setSales(product.getSales() + salesCount);
        product.setSamples(product.getSamples() + samplesCount);

        return productRepository.save(product);
    }

    // Record sales and samples given by a specific field officer for a product
    public ProductSalesRecord recordProductSalesAndSamples(Long officerId, Long productId,
                                                          int salesCount, int samplesCount){
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductSalesRecord record = new ProductSalesRecord();
        record.setOfficer(officer);
        record.setProduct(product);
        record.setSalesCount(salesCount);
        record.setSamplesGiven(samplesCount);
        record.setRecordDate(LocalDateTime.now());

        // Also update the product totals
        product.setSales(product.getSales() + salesCount);
        product.setSamples(product.getSamples() + samplesCount);
        productRepository.save(product);

        return productSalesRecordRepository.save(record);
    }

    // Get all tasks for an officer as DTOs
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksForOfficer(Long officerId) {
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        List<Task> tasks = taskRepository.findByAssignedToOfficer(officer);
        return tasks.stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Get tasks by status for an officer as DTOs
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByStatusForOfficer(Long officerId, String status) {
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        List<Task> tasks = taskRepository.findByAssignedToOfficerAndStatus(officer, status);
        return tasks.stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Update task status to COMPLETED and return DTO
    @Transactional
    public TaskDTO updateTaskStatus(Long taskId, Long officerId, String status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Verify that the task is assigned to the requesting officer
        if (!task.getAssignedToOfficer().getId().equals(officerId)) {
            throw new RuntimeException("Task is not assigned to this officer");
        }

        // Update status
        task.setStatus(status);

        // Set completed date if status is COMPLETED
        if ("COMPLETED".equalsIgnoreCase(status)) {
            task.setCompletedDate(LocalDateTime.now());
        }

        Task savedTask = taskRepository.save(task);
        return TaskDTO.fromEntity(savedTask);
    }
}

