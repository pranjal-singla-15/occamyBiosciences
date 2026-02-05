package com.occamy.occamyBiosciences.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficerMeetingsSalesDTO {
    private Long officerId;
    private String officerName;
    private Long totalMeetings;
    private Double totalSales;
}

