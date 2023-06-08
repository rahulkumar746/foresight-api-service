package com.doceree.foresightService.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Table(value = "advertiser_plan")
public class AdvertiserPlanV2 {
    @Id
    @Column("plan_id")
    @Generated
    private long planId;
    @Column("plan_name")
    private String planName;
    @Column("objectives")
    private Set<String> objectives;
    @Column("start_date")
    private String startDate;
    @Column("end_date")
    private String endDate;
    @Column("duration_in_months")
    private int durationInMonths;
    @Column("targeting_type")
    private String targetingType;
    @Column("targeting_ids")
    private Set<String> targetingIds;
    @Column("targeting_ids_v2")
    private List<String> targetingIdsV2;
    @Column("brand_id")
    private String brandId;
    @Column("advertiser_id")
    private String advertiserId;
    @Column("budget")
    private double budget;
    @Column("expected_impressions")
    private long expectedImpressions;
    @Column("expected_spend")
    private double expectedSpend;
    @Column("expected_reach")
    private long expectedReach;
    @Column("is_active")
    private boolean active;
    @Column("deleted")
    private boolean deleted = false;
    @Column("created_at")
    private Timestamp createdAt;
    @Column("updated_at")
    private Timestamp updatedAt;
    @Column("created_by")
    private String createdBy;
    @Column("updated_by")
    private String updatedBy;
    @Column("cpm")
    private double cpm;
    @Column("status")
    private int status;
    @Column("ui_request_json")
    private String uiRequestJson;
    @Column("ui_response_json")
    private String uiResponseJson;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdvertiserPlanV2 that = (AdvertiserPlanV2) o;
        return planId == that.planId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId);
    }
}
