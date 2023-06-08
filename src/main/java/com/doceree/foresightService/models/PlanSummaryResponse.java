package com.doceree.foresightService.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PlanSummaryResponse {
    private int planId;
    private String brandId;
    private String planName;
    private double cpmValue;
    private int reach;
    private int impression;
    private double budget;
    private String planResponseUiJson;
}
