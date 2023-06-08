package com.doceree.foresightService.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;
import java.util.Set;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Table(value = "child_advertiser_plan")
public class ChildAdvertiserPlan {

    @Id
    @Column("child_plan_id")
    @Generated
    private long childPlanId;
    @Column("parent_plan_id")
    private long parentPlanId;
    @Column("tactic_name")
    private String tacticName;
    @Column("tactic_type")
    private String tacticType;
    @Column("start_date")
    private String startDate;
    @Column("end_date")
    private String endDate;
    @Column("budget")
    private double budget;
    @Column("expected_impressions")
    private long expectedImpressions;
    @Column("expected_spend")
    private double expectedSpend;
    @Column("expected_reach")
    private long expectedReach;
    @Column("header_graph_data")
    private String headerGraphData;
    @Column("distribution_graph_data")
    private String distributionGraphData;
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
    @Column("platform_type")
    private Set<String> platformType;

    @Column("target_type")
    private Set<String> targetType;
    @Column("network_type")
    private Set<String> networkType;
    @Column("ui_request_json")
    private String uiRequestJson;
    @ReadOnlyProperty
    private Set<String> npiList;
}
