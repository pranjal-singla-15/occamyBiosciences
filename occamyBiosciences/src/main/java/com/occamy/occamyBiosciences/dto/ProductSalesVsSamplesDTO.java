package com.occamy.occamyBiosciences.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSalesVsSamplesDTO {
    private Long officerId;
    private String officerName;
    private Long productId;
    private String productName;
    private int totalSales;
    private int totalSamples;
    private double samplesConversionRate; // (sales/samples) * 100
}

