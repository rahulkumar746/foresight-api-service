package com.doceree.foresightService.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanWiseSubplan {

    private AdvertiserPlanV2 parentPlan;
    private Set<ChildAdvertiserPlan> childPlans = new HashSet<>();
    @JsonIgnore
    private Map<String, ChildAdvertiserPlan> childPlanMap = new HashMap<>();
}
