package com.occamy.occamyBiosciences.repository;

import com.occamy.occamyBiosciences.entity.Attendance;
import com.occamy.occamyBiosciences.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendenceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findTopByUserOrderByStartTimeDesc(User user);
}
