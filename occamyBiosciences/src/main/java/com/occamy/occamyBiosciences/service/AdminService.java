package com.occamy.occamyBiosciences.service;

import com.occamy.occamyBiosciences.dto.OfficerMeetingsSalesDTO;
import com.occamy.occamyBiosciences.dto.ProductSalesVsSamplesDTO;
import com.occamy.occamyBiosciences.dto.TaskDTO;
import com.occamy.occamyBiosciences.entity.Meeting;
import com.occamy.occamyBiosciences.entity.Product;
import com.occamy.occamyBiosciences.entity.ProductSalesRecord;
import com.occamy.occamyBiosciences.entity.Task;
import com.occamy.occamyBiosciences.entity.User;
import com.occamy.occamyBiosciences.enums.MeetingType;
import com.occamy.occamyBiosciences.enums.Role;
import com.occamy.occamyBiosciences.repository.MeetingRepository;
import com.occamy.occamyBiosciences.repository.ProductRepository;
import com.occamy.occamyBiosciences.repository.ProductSalesRecordRepository;
import com.occamy.occamyBiosciences.repository.TaskRepository;
import com.occamy.occamyBiosciences.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductSalesRecordRepository productSalesRecordRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User createFieldOfficer(Long adminId, String userName, String password, String phoneNumber) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("User is not an admin");
        }

        if (userRepository.findByUserName(userName).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User fieldOfficer = new User();
        fieldOfficer.setUserName(userName);
        fieldOfficer.setPassword(passwordEncoder.encode(password));
        fieldOfficer.setPhoneNumber(phoneNumber);
        fieldOfficer.setRole(Role.USER);
        fieldOfficer.setManagedBy(admin);

        return userRepository.save(fieldOfficer);
    }

    public List<User> getMyFieldOfficers(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("User is not an admin");
        }

        return userRepository.findByManagedBy(admin);
    }

    public User createAdmin(String userName, String password, String phoneNumber) {
        // Check if username already exists
        if (userRepository.findByUserName(userName).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User admin = new User();
        admin.setUserName(userName);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setPhoneNumber(phoneNumber);
        admin.setRole(Role.ADMIN);
        return userRepository.save(admin);
    }

    // Get sales vs samples report for a specific field officer
    public List<ProductSalesVsSamplesDTO> getSalesVsSamplesForOfficer(Long officerId){
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        List<ProductSalesRecord> records = productSalesRecordRepository.findByOfficerId(officerId);

        // Group by product and calculate totals
        Map<Long, ProductSalesVsSamplesDTO> productMap = new HashMap<>();

        for (ProductSalesRecord record : records) {
            Long productId = record.getProduct().getId();

            if (!productMap.containsKey(productId)) {
                ProductSalesVsSamplesDTO dto = new ProductSalesVsSamplesDTO();
                dto.setOfficerId(officerId);
                dto.setOfficerName(officer.getUserName());
                dto.setProductId(productId);
                dto.setProductName(record.getProduct().getName());
                dto.setTotalSales(0);
                dto.setTotalSamples(0);
                productMap.put(productId, dto);
            }

            ProductSalesVsSamplesDTO dto = productMap.get(productId);
            dto.setTotalSales(dto.getTotalSales() + record.getSalesCount());
            dto.setTotalSamples(dto.getTotalSamples() + record.getSamplesGiven());
        }

        // Calculate conversion rates
        for (ProductSalesVsSamplesDTO dto : productMap.values()) {
            if (dto.getTotalSamples() > 0) {
                double rate = (dto.getTotalSales() * 100.0) / dto.getTotalSamples();
                dto.setSamplesConversionRate(Math.round(rate * 100.0) / 100.0);
            } else {
                dto.setSamplesConversionRate(0.0);
            }
        }

        return new ArrayList<>(productMap.values());
    }

    // Get sales vs samples report for all field officers (grouped by officer and product)
    public List<ProductSalesVsSamplesDTO> getSalesVsSamplesForAllOfficers(){
        List<ProductSalesRecord> allRecords = productSalesRecordRepository.findAll();

        // Group by officer and product
        Map<String, ProductSalesVsSamplesDTO> reportMap = new HashMap<>();

        for (ProductSalesRecord record : allRecords) {
            String key = record.getOfficer().getId() + "_" + record.getProduct().getId();

            if (!reportMap.containsKey(key)) {
                ProductSalesVsSamplesDTO dto = new ProductSalesVsSamplesDTO();
                dto.setOfficerId(record.getOfficer().getId());
                dto.setOfficerName(record.getOfficer().getUserName());
                dto.setProductId(record.getProduct().getId());
                dto.setProductName(record.getProduct().getName());
                dto.setTotalSales(0);
                dto.setTotalSamples(0);
                reportMap.put(key, dto);
            }

            ProductSalesVsSamplesDTO dto = reportMap.get(key);
            dto.setTotalSales(dto.getTotalSales() + record.getSalesCount());
            dto.setTotalSamples(dto.getTotalSamples() + record.getSamplesGiven());
        }

        // Calculate conversion rates
        for (ProductSalesVsSamplesDTO dto : reportMap.values()) {
            if (dto.getTotalSamples() > 0) {
                double rate = (dto.getTotalSales() * 100.0) / dto.getTotalSamples();
                dto.setSamplesConversionRate(Math.round(rate * 100.0) / 100.0);
            } else {
                dto.setSamplesConversionRate(0.0);
            }
        }

        return new ArrayList<>(reportMap.values());
    }

    // Get sales vs samples for a specific product across all officers
    public List<ProductSalesVsSamplesDTO> getSalesVsSamplesForProduct(Long productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductSalesRecord> records = productSalesRecordRepository.findByProductId(productId);

        // Group by officer
        Map<Long, ProductSalesVsSamplesDTO> officerMap = new HashMap<>();

        for (ProductSalesRecord record : records) {
            Long officerId = record.getOfficer().getId();

            if (!officerMap.containsKey(officerId)) {
                ProductSalesVsSamplesDTO dto = new ProductSalesVsSamplesDTO();
                dto.setOfficerId(officerId);
                dto.setOfficerName(record.getOfficer().getUserName());
                dto.setProductId(productId);
                dto.setProductName(product.getName());
                dto.setTotalSales(0);
                dto.setTotalSamples(0);
                officerMap.put(officerId, dto);
            }

            ProductSalesVsSamplesDTO dto = officerMap.get(officerId);
            dto.setTotalSales(dto.getTotalSales() + record.getSalesCount());
            dto.setTotalSamples(dto.getTotalSamples() + record.getSamplesGiven());
        }

        // Calculate conversion rates
        for (ProductSalesVsSamplesDTO dto : officerMap.values()) {
            if (dto.getTotalSamples() > 0) {
                double rate = (dto.getTotalSales() * 100.0) / dto.getTotalSamples();
                dto.setSamplesConversionRate(Math.round(rate * 100.0) / 100.0);
            } else {
                dto.setSamplesConversionRate(0.0);
            }
        }

        return new ArrayList<>(officerMap.values());
    }

    // Assign a task to a specific field officer
    @Transactional
    public Task assignTaskToFieldOfficer(Long adminId, Long fieldOfficerId, String title, String description, LocalDateTime dueDate) {
        // Verify admin exists and has admin role
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("User is not an admin");
        }

        // Verify field officer exists
        User fieldOfficer = userRepository.findById(fieldOfficerId)
                .orElseThrow(() -> new RuntimeException("Field officer not found"));

        // Verify that the field officer is managed by this admin
        if (fieldOfficer.getManagedBy() == null || !fieldOfficer.getManagedBy().getId().equals(adminId)) {
            throw new RuntimeException("Field officer is not managed by this admin");
        }

        // Create and save the task
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setAssignedToOfficer(fieldOfficer);
        task.setAssignedByAdmin(admin);
        task.setAssignedDate(LocalDateTime.now());
        task.setDueDate(dueDate);
        task.setStatus("PENDING");

        return taskRepository.save(task);
    }

    // Get all tasks assigned by a specific admin
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksAssignedByAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("User is not an admin");
        }

        // Use ID-based query for more reliable results
        List<Task> tasks = taskRepository.findByAssignedByAdminId(adminId);
        return tasks.stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Get all tasks assigned to a specific field officer
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksForFieldOfficer(Long adminId, Long fieldOfficerId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("User is not an admin");
        }

        User fieldOfficer = userRepository.findById(fieldOfficerId)
                .orElseThrow(() -> new RuntimeException("Field officer not found"));

        // Verify that the field officer is managed by this admin
        if (fieldOfficer.getManagedBy() == null || !fieldOfficer.getManagedBy().getId().equals(adminId)) {
            throw new RuntimeException("Field officer is not managed by this admin");
        }

        // Use ID-based query for more reliable results
        List<Task> tasks = taskRepository.findByAssignedToOfficerId(fieldOfficerId);
        return tasks.stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Get meetings vs sales data for all field officers
    public List<OfficerMeetingsSalesDTO> getMeetingsVsSalesData(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("User is not an admin");
        }

        // Get all field officers managed by this admin
        List<User> fieldOfficers = userRepository.findByManagedBy(admin);
        List<OfficerMeetingsSalesDTO> data = new ArrayList<>();

        for (User officer : fieldOfficers) {
            // Count meetings
            Long meetingCount = meetingRepository.countByOfficerId(officer.getId());

            // Calculate total sales from product sales records
            List<ProductSalesRecord> salesRecords = productSalesRecordRepository.findByOfficerId(officer.getId());
            double totalSales = salesRecords.stream()
                    .mapToDouble(record -> {
                        Product product = record.getProduct();
                        // Assuming each sale is worth a certain amount (you can modify this logic)
                        return record.getSalesCount() * 100.0; // Example: 100 rupees per sale
                    })
                    .sum();

            OfficerMeetingsSalesDTO dto = new OfficerMeetingsSalesDTO(
                    officer.getId(),
                    officer.getUserName(),
                    meetingCount != null ? meetingCount : 0L,
                    totalSales
            );
            data.add(dto);
        }

        return data;
    }

    // Assign a meeting to a field officer
    public Meeting assignMeetingToFieldOfficer(Long adminId, Long fieldOfficerId, MeetingType meetingType,
                                               String location, String latitude, String longitude, String notes) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("User is not an admin");
        }

        User fieldOfficer = userRepository.findById(fieldOfficerId)
                .orElseThrow(() -> new RuntimeException("Field officer not found"));

        if (fieldOfficer.getManagedBy() == null || !fieldOfficer.getManagedBy().getId().equals(adminId)) {
            throw new RuntimeException("Field officer is not managed by this admin");
        }

        // Create and save the meeting
        Meeting meeting = new Meeting();
        meeting.setMeetingType(meetingType);
        meeting.setVillage(location);
        meeting.setLatitude(latitude != null ? latitude : "0.0");
        meeting.setLongitude(longitude != null ? longitude : "0.0");
        meeting.setOfficer(fieldOfficer);

        return meetingRepository.save(meeting);
    }

    // Create a new product
    public Product createProduct(String name) {
        Product product = new Product();
        product.setName(name);
        product.setSales(0);
        product.setSamples(0);
        return productRepository.save(product);
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
