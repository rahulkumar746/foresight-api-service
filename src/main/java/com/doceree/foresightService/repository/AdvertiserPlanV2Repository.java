package com.doceree.foresightService.repository;


import com.doceree.foresightService.models.AdvertiserPlanV2;
import com.doceree.foresightService.models.PlanSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;

@Repository
@RequiredArgsConstructor
public class AdvertiserPlanV2Repository {
    @Autowired
    R2dbcEntityTemplate r2dbcEntityTemplate;

    public Mono<AdvertiserPlanV2> findByPlanId(Long id) {
        return this.r2dbcEntityTemplate.selectOne(Query.query(where("plan_id").is(id)), AdvertiserPlanV2.class);
    }
    public Mono<Integer> updatePlanV2(AdvertiserPlanV2 advertiserPlanV2) {
        return this.r2dbcEntityTemplate.update(
                Query.query(where("plan_id").is(advertiserPlanV2.getPlanId())),
                Update.update("plan_name", advertiserPlanV2.getPlanName())
                        .set("start_date", advertiserPlanV2.getStartDate())
                        .set("end_date", advertiserPlanV2.getEndDate())
                        .set("duration_in_Months", advertiserPlanV2.getDurationInMonths())
                        .set("budget", advertiserPlanV2.getBudget())
                        .set("expected_reach", advertiserPlanV2.getExpectedReach())
                        .set("expected_impressions", advertiserPlanV2.getExpectedImpressions())
                        .set("expected_spend", advertiserPlanV2.getExpectedSpend())
                        .set("advertiser_id", advertiserPlanV2.getAdvertiserId())
                        .set("deleted", advertiserPlanV2.isDeleted())
                        .set("brand_id", advertiserPlanV2.getBrandId())
                        .set("targeting_ids", advertiserPlanV2.getTargetingIds() != null ? advertiserPlanV2.getTargetingIds().toArray(new String[advertiserPlanV2.getTargetingIds().size()])  : null)
                        .set("targeting_ids_v2", advertiserPlanV2.getTargetingIdsV2() != null ? advertiserPlanV2.getTargetingIdsV2().toArray(new String[advertiserPlanV2.getTargetingIdsV2().size()]) : null)
                        .set("objectives", advertiserPlanV2.getObjectives() != null ? advertiserPlanV2.getObjectives().toArray(new String[advertiserPlanV2.getObjectives().size()]) : null)
                        .set("created_by", advertiserPlanV2.getCreatedBy())
                        .set("updated_by", advertiserPlanV2.getUpdatedBy())
                        .set("cpm", advertiserPlanV2.getCpm())
                        .set("status", advertiserPlanV2.getStatus())
                        .set("ui_request_json", advertiserPlanV2.getUiRequestJson())
                        .set("ui_response_json", advertiserPlanV2.getUiResponseJson()),
                AdvertiserPlanV2.class
        );
    }
    public Mono<AdvertiserPlanV2> createPlanV2(AdvertiserPlanV2 advertiserPlanV2) {
        return this.r2dbcEntityTemplate.insert(advertiserPlanV2);
    }

    public Mono<Integer> updatePredictionDataInPlanByPlanId(PlanSummaryResponse summary, long parentPlanId) {
        return this.r2dbcEntityTemplate.update(
                Query.query(where("plan_id").is(parentPlanId)),
                Update.update("expected_reach", summary.getReach())
                        .set("expected_impressions", summary.getImpression())
                        .set("expected_spend", summary.getBudget())
                        .set("cpm", summary.getCpmValue())
                        .set("ui_response_json", summary.getPlanResponseUiJson()),
                AdvertiserPlanV2.class
        );
    }

    public Mono<Integer>  updatePredictionDataInPlanByPlanNameAndBrandId(PlanSummaryResponse summary) {
        return this.r2dbcEntityTemplate.update(
                Query.query(where("plan_name").is(summary.getPlanName()).and("brand_id").is(summary.getBrandId())),
                Update.update("expected_reach", summary.getReach())
                        .set("expected_impressions", summary.getImpression())
                        .set("expected_spend", summary.getBudget())
                        .set("cpm", summary.getCpmValue())
                        .set("ui_response_json", summary.getPlanResponseUiJson()),
                AdvertiserPlanV2.class
        );
    }

    public Mono<AdvertiserPlanV2> findByPlanByBrandIdAndPlanName(String planName, String brandId) {
        return this.r2dbcEntityTemplate.selectOne(Query.query(where("plan_name").is(planName).and("brand_id").is(brandId)), AdvertiserPlanV2.class);
    }

    public Mono<Integer> updatePlanStatusInPlanByPlanId(long parentPlanId) {
        return this.r2dbcEntityTemplate.update(
                Query.query(where("plan_id").is(parentPlanId)),
                Update.update("status",1),
                AdvertiserPlanV2.class
        );
    }

}
