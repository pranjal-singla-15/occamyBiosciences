package com.occamy.occamyBiosciences.repository;

import com.occamy.occamyBiosciences.entity.Meeting;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByOfficerId(Long officerId);

    @Query("SELECT COUNT(m) FROM Meeting m WHERE m.officer.id = :officerId")
    Long countByOfficerId(Long officerId);
}
