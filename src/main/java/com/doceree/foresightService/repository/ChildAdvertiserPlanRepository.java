package com.doceree.foresightService.repository;

import com.doceree.foresightService.models.ChildAdvertiserPlan;
import com.doceree.foresightService.models.DsCreateUpdateTacticResponse;
import com.doceree.foresightService.utils.ApplicationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;


@Repository
@RequiredArgsConstructor
public class ChildAdvertiserPlanRepository {

    @Autowired
    R2dbcEntityTemplate r2dbcEntityTemplate;

    public Flux<ChildAdvertiserPlan> findPlanByDeletedStatus(Boolean status) {
        return this.r2dbcEntityTemplate.select(Query.query(where("deleted").is(status)), ChildAdvertiserPlan.class);
    }

    public Mono<ChildAdvertiserPlan> findByChildPlanByNameAndParentPlanId(String name, long parentPlanId) {
        return this.r2dbcEntityTemplate.selectOne(Query.query(where(ApplicationConstants.TACTIC_NAME_COLUMN).is(name).and(ApplicationConstants.PARENT_PLAN_ID_COLUMN).is(parentPlanId)), ChildAdvertiserPlan.class);
    }
    public Mono<ChildAdvertiserPlan> createChildPlan(ChildAdvertiserPlan childadvertiserPlan) {
        return this.r2dbcEntityTemplate.insert(childadvertiserPlan);
    }

    public Mono<ChildAdvertiserPlan> findChildPlanByChildPlanId(Long id) {
        return this.r2dbcEntityTemplate.selectOne(Query.query(where("child_plan_id").is(id)), ChildAdvertiserPlan.class);
    }

    public Mono<Integer> updateChildPlan(ChildAdvertiserPlan childAdvertiserPlan) {
        return this.r2dbcEntityTemplate.update(
                Query.query(where(ApplicationConstants.PARENT_PLAN_ID_COLUMN).is(childAdvertiserPlan.getParentPlanId()).and(ApplicationConstants.TACTIC_NAME_COLUMN).is(childAdvertiserPlan.getTacticName())),
                Update.update("start_date", childAdvertiserPlan.getStartDate())
                        .set("end_date", childAdvertiserPlan.getEndDate())
                        .set("budget", childAdvertiserPlan.getBudget())
                        .set("expected_impressions", childAdvertiserPlan.getExpectedImpressions())
                        .set("expected_spend", childAdvertiserPlan.getExpectedSpend())
                        .set("expected_reach", childAdvertiserPlan.getExpectedReach())
                        .set("deleted", childAdvertiserPlan.isDeleted())
                        .set("header_graph_data", childAdvertiserPlan.getHeaderGraphData())
                        .set("is_active", childAdvertiserPlan.isActive())
                        .set("created_by", childAdvertiserPlan.getCreatedBy())
                        .set("updated_by", childAdvertiserPlan.getUpdatedBy())
                        .set("platform_type", childAdvertiserPlan.getPlatformType().toArray(new String[childAdvertiserPlan.getPlatformType().size()]))
                        .set("target_type", childAdvertiserPlan.getTargetType() != null ? childAdvertiserPlan.getTargetType().toArray(new String[childAdvertiserPlan.getTargetType().size()]) : null)
                        .set("network_type", childAdvertiserPlan.getNetworkType().toArray(new String[childAdvertiserPlan.getNetworkType().size()]))
                        .set("ui_request_json", childAdvertiserPlan.getUiRequestJson()),
                ChildAdvertiserPlan.class
        );
    }

    public Mono<Integer> updateDistributionGraphDataInChildPlanById(String distributionGraphJson, long childPlanId) {
        return this.r2dbcEntityTemplate.update(
                Query.query(where("child_plan_id").is(childPlanId)
                ), Update.update("distribution_graph_data", distributionGraphJson), ChildAdvertiserPlan.class);
    }

    public Mono<Integer> updatePredictionDataInChildPlanByNameAndParentPlanId(DsCreateUpdateTacticResponse tacticResponse, long parentPlanId) {
        return this.r2dbcEntityTemplate.update(
                Query.query(where(ApplicationConstants.TACTIC_NAME_COLUMN).is(tacticResponse.getName()).and(ApplicationConstants.PARENT_PLAN_ID_COLUMN).is(parentPlanId)),
                Update.update("expected_reach", tacticResponse.getPredictedReach())
                        .set("expected_impressions", tacticResponse.getPredictedImpression())
                        .set("expected_spend", tacticResponse.getPredictedSpend())
                        .set("header_graph_data", tacticResponse.getHeaderGraphs()),
                ChildAdvertiserPlan.class
        );
    }

}
