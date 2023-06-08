package com.doceree.foresightService.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class DsCreateUpdatePlanResponse {

    private DsCreateUpdateTacticResponse[] tactics;

    private PlanSummaryResponse summary;
}
