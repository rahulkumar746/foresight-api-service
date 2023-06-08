package com.doceree.foresightService.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Slf4j
public class ParentAndChildPlansGroup {

    private AdvertiserPlanV2 parentPlan;
    private List<ChildAdvertiserPlan> childPlans;
}
