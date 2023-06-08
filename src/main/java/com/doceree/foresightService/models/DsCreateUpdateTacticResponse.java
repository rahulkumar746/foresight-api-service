package com.doceree.foresightService.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class DsCreateUpdateTacticResponse {

    private String[] error;
    private int childPlanId;
    private String name;
    private int predictedReach;
    private int predictedImpression;
    private double predictedSpend;
    private String headerGraphs;
}
