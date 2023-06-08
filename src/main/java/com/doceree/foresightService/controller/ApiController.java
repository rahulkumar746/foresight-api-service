package com.doceree.foresightService.controller;


import com.doceree.foresightService.models.*;
import com.doceree.foresightService.service.PlannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/")
public class ApiController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    @Autowired
    PlannerService plannerService;


    @GetMapping("memoryStats")
    public MemoryStats getMemoryStatistics() {
        MemoryStats stats = new MemoryStats();
        stats.setTotalAllocatedMemory(Runtime.getRuntime().totalMemory());
        stats.setMaxAllocatedMemory(Runtime.getRuntime().maxMemory());
        stats.setTotalFreeMemore(Runtime.getRuntime().freeMemory());
        stats.setTotalUsedMemory(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        return stats;
    }

    @RequestMapping(method = RequestMethod.GET, path = "findPlanByPlanId")
    public PlanWiseSubplan findPlanByPLanId(@RequestParam long planId) {
        return plannerService.getPlanWiseSubplans(planId);
    }
    @RequestMapping(method = RequestMethod.POST, path = "createOrUpdatePlan")
    public AdvertiserPlanV2 createOrUpdatePlan(@RequestBody AdvertiserPlanV2 advertiserPlanv2) {
        AdvertiserPlanV2 plan;
        logger.debug(advertiserPlanv2.toString());
        if (advertiserPlanv2.getPlanId() != 0) {
            plan = plannerService.updatePlanV2(advertiserPlanv2);
        } else {
            plan = plannerService.createPlanV2(advertiserPlanv2);
        }
        return plan;
    }
    @RequestMapping(method = RequestMethod.POST, path = "createOrUpdateChildPlan")
    public ChildAdvertiserPlan createOrUpdateChildPlan(@RequestBody ChildAdvertiserPlan childAdvertiserPlan) {
        ChildAdvertiserPlan plan = childAdvertiserPlan;
        PlanWiseSubplan planWiseSubplan = plannerService.getPlanWiseSubplans(childAdvertiserPlan.getParentPlanId());
        if (planWiseSubplan != null && planWiseSubplan.getChildPlanMap().containsKey(childAdvertiserPlan.getTacticName())) {
            plan = plannerService.updateChildPlan(plan);
        } else {
            plan = plannerService.createChildPlan(childAdvertiserPlan);
        }
        return plan;
    }
    @RequestMapping(method = RequestMethod.POST, path = "updateFieldInChildPlan")
    public String updateFieldInChildPlan(@RequestParam String childPlanId, @RequestParam String fieldToUpdate, @RequestBody String distributionGraphJson) {
        String message;
        if("distribution_graph_data".equals(fieldToUpdate) && plannerService.updateChildPlanField(Long.parseLong(childPlanId), distributionGraphJson))
            message = "Child Plan updated Successfully, Child Plan Id : " + childPlanId + ", Field Updated : " + fieldToUpdate;
        else
            message = "Child plan updation failed. Child Plan Id : " + childPlanId;
        logger.info(message);
        return message;
    }
    @RequestMapping(method = RequestMethod.POST, path = "createOrUpdateParentAndChildPlan")
    public ParentAndChildPlansGroup createOrUpdateParentAndChildPlan(@RequestBody ParentAndChildPlansGroup requestParentAndChildPlansGroup) {
        try {
            AdvertiserPlanV2 responseParentPlan = createOrUpdatePlan(requestParentAndChildPlansGroup.getParentPlan());
            Set<ChildAdvertiserPlan> plansTobeUpdatedOrCreated = new HashSet<>();
            if(responseParentPlan != null) {
                PlanWiseSubplan planWiseSubPlans = plannerService.getPlanWiseSubplans(responseParentPlan.getPlanId());
                Set<ChildAdvertiserPlan> existingChildPlans = planWiseSubPlans != null ? planWiseSubPlans.getChildPlans() : null;
                Map<String, ChildAdvertiserPlan> mapOfInputChildPlans = plannerService.convertChildPlansToMap(requestParentAndChildPlansGroup.getChildPlans());
                if(existingChildPlans != null) {
                    for (ChildAdvertiserPlan existingChildPLan : existingChildPlans) {
                        if (!mapOfInputChildPlans.containsKey(existingChildPLan.getTacticName().toLowerCase())) {
                            existingChildPLan.setDeleted(true);
                            plansTobeUpdatedOrCreated.add(existingChildPLan);
                        } else {
                            mapOfInputChildPlans.get(existingChildPLan.getTacticName().toLowerCase()).setParentPlanId(responseParentPlan.getPlanId());
                            plansTobeUpdatedOrCreated.add(mapOfInputChildPlans.get(existingChildPLan.getTacticName().toLowerCase()));
                            mapOfInputChildPlans.put(existingChildPLan.getTacticName().toLowerCase(), null);
                        }
                    }
                }
                for(Map.Entry<String, ChildAdvertiserPlan> entry : mapOfInputChildPlans.entrySet()) {
                    if(entry.getValue() != null) {
                        entry.getValue().setParentPlanId(responseParentPlan.getPlanId());
                        plansTobeUpdatedOrCreated.add(entry.getValue());
                    }
                }
            }
            List<ChildAdvertiserPlan> responseChildPlans = new ArrayList<>();
            for(ChildAdvertiserPlan planToCreateOrUpdate : plansTobeUpdatedOrCreated) {
                ChildAdvertiserPlan responseChildPlan = createOrUpdateChildPlan(planToCreateOrUpdate);
                if(responseChildPlan != null && !responseChildPlan.isDeleted())
                    responseChildPlans.add(responseChildPlan);
            }
            ParentAndChildPlansGroup responseParentAndChildPlansGroup = new ParentAndChildPlansGroup();
            responseParentAndChildPlansGroup.setParentPlan(responseParentPlan);
            responseParentAndChildPlansGroup.setChildPlans(responseChildPlans);
            return responseParentAndChildPlansGroup;
        } catch (Exception e) {
            logger.error("Exception occurred while creating/updating plan. ", e);
            return new ParentAndChildPlansGroup();
        }
    }
    @RequestMapping(method = RequestMethod.GET, path = "canPlanBeProcessed")
    public boolean canPlanBeProcessed(@RequestParam String planName, @RequestParam String brandId, @RequestParam String planId) {
        return plannerService.canPlanBeProcessed(planName, brandId, planId);
    }
    @RequestMapping(method = RequestMethod.POST, path = "saveOrUpdatePredictionData")
    public SavePredictionDataResponse saveOrUpdatePredictionData(@RequestBody DsCreateUpdatePlanResponse dsCreateUpdatePlanResponse) {
        SavePredictionDataResponse response = plannerService.updatePredictionDataInPlanV2(dsCreateUpdatePlanResponse);
        logger.info(response.getMessage());
        return response;
    }
    @RequestMapping(method = RequestMethod.GET, path = "getParentPlanResponseJson")
    public String getParentPlanRequestJsonByPlanNameOrPlanId(@RequestParam String planName, @RequestParam String brandId, @RequestParam Integer planId) {
        logger.info("getParentPlanResponseJson request received for plan id " + planId + " , plan name : " + planName + ", brand id : " + brandId);
        if (planId != null && planId > 0) {
            AdvertiserPlanV2 planByIdV2 = plannerService.getPlanByIdV2(planId);
            return planByIdV2 != null ? planByIdV2.getUiResponseJson() : null;
        } else {
            AdvertiserPlanV2 plan = plannerService.getPlanByPlanNameAndBrandId(planName, brandId);
            if (plan != null)
                return plan.getUiResponseJson();
        }
        return null;
    }
    @RequestMapping(method = RequestMethod.GET, path = "getParentPlanByChildPlanId")
    public PlanWiseSubplan getParentPlanIdFromChildPlanId(@RequestParam long planId) {
        ChildAdvertiserPlan childPlan = plannerService.getChildPlanByChildId(planId);
        long parentPlanId = childPlan.getParentPlanId();
        return plannerService.getPlanWiseSubplans(parentPlanId);
    }
    @RequestMapping(method = RequestMethod.GET, path = "findChildPlanByChildId")
    public ChildAdvertiserPlan findChildPlanByChildId(@RequestParam long childPlanId) {
        return plannerService.getChildPlanByChildId(childPlanId);
    }
    @RequestMapping(method = RequestMethod.GET, path = "findChildPlanByChildIdForDistGraph")
    public ChildAdvertiserPlan findChildPlanByChildIdForDistGraph(@RequestParam long childPlanId) {
        return plannerService.getChildPlanByChildIdForDistGraph(childPlanId);
    }
    @RequestMapping(method = RequestMethod.GET, path = "getNdcCodesByBrandName")
    public Set<String> getNdcCodesByBrandName(@RequestParam String brandName) {
        Set<String> ndcCodes = plannerService.getNdcCodesByBrandName(brandName);
        return ndcCodes;
    }

    @RequestMapping(method = RequestMethod.GET, path = "getTargettingIdsfromChildPlan")
    public Set<String> getTargettingIdsfromChildPlan(@RequestParam long planId) {
        ChildAdvertiserPlan childPlan = plannerService.getChildPlanByChildId(planId);
        long parentPlanId = childPlan.getParentPlanId();
        AdvertiserPlanV2 planByIdV2 = plannerService.getPlanByIdV2(parentPlanId);
        return planByIdV2.getTargetingIds();
    }

    @RequestMapping(method = RequestMethod.GET, path = "getAdvertiserAudience")
    public NpiListAndTaggedCountsByAudienceId getAdvertiserAudience(@RequestParam String id) {
        return plannerService.getNpiListAndTaggedCountsByAudienceId(id);
    }

    @RequestMapping(method = RequestMethod.GET, path = "getParentPlansByBrandId")
    public List<AdvertiserPlanV2> getActivePlans(@RequestParam String brandId, @RequestParam String advertiserId) {
        try {
            return plannerService.getAllActivePlans(brandId, advertiserId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = "check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }

    @RequestMapping("/")
    public ResponseEntity<String> index() {
        return health();
    }


    @RequestMapping(method = RequestMethod.GET, path = "getAccountTargetedDomains")
    public Set<String> getAccountTargetedDomains(@RequestParam String id) {
        return plannerService.getAccountTargetedDomainsByAccountIds(id);
    }

    @RequestMapping(method = RequestMethod.GET, path = "getIntentTargetedDomains")
    public Set<String> getIntentTargetedDomains(@RequestParam String id) {
        return plannerService.getIntentTargetedDomainsByAccountIds(id);
    }


}
