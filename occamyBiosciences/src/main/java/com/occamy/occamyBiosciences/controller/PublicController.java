package com.occamy.occamyBiosciences.controller;

import com.occamy.occamyBiosciences.entity.User;
import com.occamy.occamyBiosciences.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/Health-check")
    public String healthCheck(){
        return "OK";
    }

    @PostMapping
    public ResponseEntity<?> createAdmin(@RequestBody User user){
        adminService.createAdmin(user.getUserName(), user.getPassword(), user.getPhoneNumber());
        return ResponseEntity.status(201).build();
    }
}
