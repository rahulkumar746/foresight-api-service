package com.doceree.foresightService.service;


import com.doceree.foresightService.cache.PlannerCache;
import com.doceree.foresightService.exception.DocereeApiException;
import com.doceree.foresightService.models.*;
import com.doceree.foresightService.repository.AdvertiserPlanV2Repository;
import com.doceree.foresightService.repository.ChildAdvertiserPlanRepository;
import com.doceree.foresightService.utils.ApplicationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Service
public class PlannerService {
    static final Logger logger = LoggerFactory.getLogger(PlannerService.class);
    @Autowired
    PlannerCache plannerCache;
    @Autowired
    ChildAdvertiserPlanRepository childAdvertiserPlanRepository;
    @Autowired
    AdvertiserPlanV2Repository advertiserPlanV2Repository;

    @Autowired
    Environment environment;

    public PlanWiseSubplan getPlanWiseSubplans(long planId) {
        return plannerCache.getPlanWiseSubplanById(planId);
    }
    public AdvertiserPlanV2 updatePlanV2(AdvertiserPlanV2 advertiserPlanv2) throws DocereeApiException {
        if (Objects.nonNull(advertiserPlanv2.getPlanName()) && Objects.nonNull(advertiserPlanv2.getBrandId())) {
            try {
                AdvertiserPlanV2 existingPlan = plannerCache.getPlanById(advertiserPlanv2.getPlanId());
                if(existingPlan != null && existingPlan.getCreatedBy() != null)
                    advertiserPlanv2.setCreatedBy(existingPlan.getCreatedBy());
                boolean isPLanUpdated = advertiserPlanV2Repository.updatePlanV2(advertiserPlanv2).toFuture().get() == 1;
                AdvertiserPlanV2 updatedPlan = advertiserPlanV2Repository.findByPlanId(advertiserPlanv2.getPlanId()).toFuture().get();
                if (isPLanUpdated) {
                    plannerCache.updatePlanV2Cache(updatedPlan);
                    plannerCache.removeExistingAndAddUpdatedPlanInUniquePlanNameAndBrandIdCache(existingPlan, updatedPlan);
                }
                return updatedPlan;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public Set<String> getAccountTargetedDomainsByAccountIds(String _id) {
        try {
            Set<String> accountTargetedDomains = new HashSet<>();
            if (StringUtils.isNotBlank(_id)) {
                Arrays.stream(StringUtils.split(_id, ApplicationConstants.COMMA)).forEach(id -> {
                    if (StringUtils.isNotBlank(id)) {
                        id = id.split(ApplicationConstants.COLON)[0];
                        AdvertiserAccountTarget advertiserAccountTarget = plannerCache.getAdvertiserAccountTargetByAccountId(id);
                        Set<String> domainsOnAccountTarget = fetchSetOfDomainsFromAdvertiserAccountTarget(advertiserAccountTarget);
                        accountTargetedDomains.addAll(domainsOnAccountTarget);
                    }
                });
                return accountTargetedDomains;
            }
        } catch (Exception e) {
            logger.error("Exception occurred while fetching Account Targeted domains by account Ids. message : " + e.getMessage(), e);
        }
        return null;
    }

    private Set<String> fetchSetOfDomainsFromAdvertiserAccountTarget(AdvertiserAccountTarget advertiserAccountTarget) {
        Set<String> domainsOnAccountTarget = new HashSet<>();
        if(advertiserAccountTarget != null && advertiserAccountTarget.getTargetInfo()!= null && advertiserAccountTarget.getTargetInfo().getAccounts() != null) {
            for (AdvertiserAccount account : advertiserAccountTarget.getTargetInfo().getAccounts()) {
                if(account.getDomains() != null && account.getDomains().size() > 0)
                    domainsOnAccountTarget.addAll(account.getDomains());
            }
        }
        return domainsOnAccountTarget;
    }

    public Set<String> getIntentTargetedDomainsByAccountIds(String _id) {
        try {
            Set<String> accountTargetedDomains = new HashSet<>();
            if (StringUtils.isNotBlank(_id)) {
                Arrays.stream(StringUtils.split(_id, ApplicationConstants.COMMA)).forEach(id -> {
                    if (StringUtils.isNotBlank(id)) {
                        id = id.split(ApplicationConstants.COLON)[0];
                        AdvertiserIntentTarget advertiserIntentTarget = plannerCache.getAdvertiserIntentTargetByIntentId(id);
                        Set<String> domainsOnAccountTarget = fetchSetOfDomainsFromAdvertiserIntentTarget(advertiserIntentTarget);
                        accountTargetedDomains.addAll(domainsOnAccountTarget);
                    }
                });
                return accountTargetedDomains;
            }
        } catch (Exception e) {
            logger.error("Exception occurred while fetching Account Targeted domains by account Ids. message : " + e.getMessage(), e);
        }
        return null;
    }

    private Set<String> fetchSetOfDomainsFromAdvertiserIntentTarget(AdvertiserIntentTarget advertiserIntentTarget) {
        Set<String> domainsOnIntentTarget = new HashSet<>();
        if(advertiserIntentTarget != null && advertiserIntentTarget.getTargetInfo()!= null) {
            if (advertiserIntentTarget.getTargetInfo().getSearchIntents() != null && advertiserIntentTarget.getTargetInfo().getSearchIntents().getTopics() != null) {
                for (TopicForIntent topic : advertiserIntentTarget.getTargetInfo().getSearchIntents().getTopics()) {
                    if (topic.getDomains() != null && topic.getDomains().size() > 0) {
                        for (DomainForIntent domainForIntent : topic.getDomains()) {
                            if(domainForIntent.getName() != null) {
                                domainsOnIntentTarget.add(domainForIntent.getName());
                            }
                        }
                    }
                }
            }
            if(advertiserIntentTarget.getTargetInfo().getRxIntentCompetitor() != null && advertiserIntentTarget.getTargetInfo().getRxIntentCompetitor().getAccounts() != null) {
                for (AdvertiserAccount account : advertiserIntentTarget.getTargetInfo().getRxIntentCompetitor().getAccounts()) {
                    if(account.getDomains() != null && account.getDomains().size() > 0)
                        domainsOnIntentTarget.addAll(account.getDomains());
                }
            }
            if(advertiserIntentTarget.getTargetInfo().getDiagnosisIntentSimilar() != null && advertiserIntentTarget.getTargetInfo().getDiagnosisIntentSimilar().getAccounts() != null) {
                for (AdvertiserAccount account : advertiserIntentTarget.getTargetInfo().getDiagnosisIntentSimilar().getAccounts()) {
                    if(account.getDomains() != null && account.getDomains().size() > 0)
                        domainsOnIntentTarget.addAll(account.getDomains());
                }
            }
        }
        return domainsOnIntentTarget;
    }


    public AdvertiserPlanV2 createPlanV2(AdvertiserPlanV2 advertiserPlanV2) throws DocereeApiException {
        if (StringUtils.isNotBlank(advertiserPlanV2.getPlanName()) && StringUtils.isNotBlank(advertiserPlanV2.getBrandId()) && plannerCache.canBeProcessed(advertiserPlanV2)) {
            try {
                AdvertiserPlanV2 advertiserPlanMono = advertiserPlanV2Repository.createPlanV2(advertiserPlanV2).toFuture().get();
                advertiserPlanMono = advertiserPlanV2Repository.findByPlanId(advertiserPlanMono.getPlanId()).toFuture().get();
                plannerCache.updatePlanV2Cache(advertiserPlanMono);
                plannerCache.addPlanInUniquePlanNameAndBrandIdCache(advertiserPlanMono);
                plannerCache.reloadPlanWithIncludedChildPlansCache();
                return advertiserPlanMono;
            } catch (DataIntegrityViolationException de) {
                logger.error("duplicate key value violates unique constraint", de);
            } catch (Exception e) {
                logger.error("Exception occurred while inserting data in advertiser_plan", e);
            }
        }
        return new AdvertiserPlanV2();
    }

    public ChildAdvertiserPlan updateChildPlan(ChildAdvertiserPlan childAdvertiserPlan) throws DocereeApiException {
        if (Objects.nonNull(childAdvertiserPlan) && childAdvertiserPlan.getParentPlanId() > 0) {
            try {
                Set<String> filteredNpiList = childAdvertiserPlan.getNpiList();
                ChildAdvertiserPlan existingChildPlan = childAdvertiserPlanRepository.findByChildPlanByNameAndParentPlanId(childAdvertiserPlan.getTacticName(), childAdvertiserPlan.getParentPlanId()).toFuture().get();
                if(existingChildPlan != null)
                    childAdvertiserPlan.setCreatedBy(existingChildPlan.getCreatedBy());
                boolean isPLanUpdated = childAdvertiserPlanRepository.updateChildPlan(childAdvertiserPlan).toFuture().get() == 1;
                ChildAdvertiserPlan updatedChildPlan = childAdvertiserPlanRepository.findByChildPlanByNameAndParentPlanId(childAdvertiserPlan.getTacticName(), childAdvertiserPlan.getParentPlanId()).toFuture().get();
                if (isPLanUpdated) {
                    plannerCache.updateChildPlanV2Cache(updatedChildPlan);
                    plannerCache.updateFilteredNpiListOnChildPlan(updatedChildPlan, filteredNpiList);
                    plannerCache.reloadPlanWithIncludedChildPlansCache();
                    return updatedChildPlan;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public ChildAdvertiserPlan createChildPlan(ChildAdvertiserPlan childadvertiserPlan) throws DocereeApiException {
        if (StringUtils.isNotBlank(childadvertiserPlan.getTacticName()) && childadvertiserPlan.getParentPlanId() != 0) {
            try {
                Set<String> filteredNpiList = childadvertiserPlan.getNpiList();
                ChildAdvertiserPlan childAdvertiserPlan = childAdvertiserPlanRepository.createChildPlan(childadvertiserPlan).toFuture().get();
                childAdvertiserPlan = childAdvertiserPlanRepository.findChildPlanByChildPlanId(childAdvertiserPlan.getChildPlanId()).toFuture().get();
                plannerCache.updateChildPlanV2Cache(childAdvertiserPlan);
                plannerCache.updateFilteredNpiListOnChildPlan(childAdvertiserPlan, filteredNpiList);
                plannerCache.reloadPlanWithIncludedChildPlansCache();
                return childAdvertiserPlan;
            } catch (DataIntegrityViolationException de) {
                throw new DocereeApiException("duplicate key value violates unique constraint");
            } catch (Exception e) {
                throw new DocereeApiException(e.getMessage());
            }
        }
        return new ChildAdvertiserPlan();
    }

    public boolean updateChildPlanField(long childPlanId, String distributionGraphJson) {
        if (childPlanId > 0) {
            try {
                boolean isChildPLanUpdated = childAdvertiserPlanRepository.updateDistributionGraphDataInChildPlanById(distributionGraphJson, childPlanId).toFuture().get() == 1;
                ChildAdvertiserPlan childAdvertiserPlan = childAdvertiserPlanRepository.findChildPlanByChildPlanId(childPlanId).toFuture().get();
                if (isChildPLanUpdated) {
                    plannerCache.updateChildPlanV2Cache(childAdvertiserPlan);
                    plannerCache.reloadPlanWithIncludedChildPlansCache();
                    return true;
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw new DocereeApiException(e.getMessage());
            }
        }
        return false;
    }

    public Map<String, ChildAdvertiserPlan> convertChildPlansToMap(List<ChildAdvertiserPlan> childPlans) {
        Map<String, ChildAdvertiserPlan> map = new HashMap<>();
        for (ChildAdvertiserPlan childAdvertiserPlan : childPlans)
            map.put(childAdvertiserPlan.getTacticName().toLowerCase(), childAdvertiserPlan);
        return map;
    }

    public boolean canPlanBeProcessed(String planName, String brandId, String planId) {
        AdvertiserPlanV2 advertiserPlanV2 = new AdvertiserPlanV2();
        advertiserPlanV2.setPlanName(planName);
        advertiserPlanV2.setBrandId(brandId);
        advertiserPlanV2.setPlanId(Long.parseLong(planId));
        return plannerCache.canBeProcessed(advertiserPlanV2);
    }

    public SavePredictionDataResponse updatePredictionDataInPlanV2(DsCreateUpdatePlanResponse dsCreateUpdatePlanResponse) {
        AdvertiserPlanV2 updatedPlan = null;
        long parentPlanId = 0;
        String email = null;
        String name = null;
        String planName = null;
        String brandId = null;
        if(dsCreateUpdatePlanResponse.getSummary() != null) {
            parentPlanId = dsCreateUpdatePlanResponse.getSummary().getPlanId() > 0 ? dsCreateUpdatePlanResponse.getSummary().getPlanId() : getParentPlanId(dsCreateUpdatePlanResponse);
            updatedPlan = updatePredictionDataInParentPlan(dsCreateUpdatePlanResponse.getSummary(), parentPlanId);
            String[] emailName = null;
            if(updatedPlan != null ) {
                emailName = StringUtils.isNotBlank(updatedPlan.getUpdatedBy()) ? updatedPlan.getUpdatedBy().split(ApplicationConstants.COMMA) :
                        StringUtils.isNotBlank(updatedPlan.getCreatedBy()) ? updatedPlan.getCreatedBy().split(ApplicationConstants.COMMA) : null;
            }
            if (emailName != null) {
                if (emailName.length > 0)
                    email = emailName[0];
                if (emailName.length > 1)
                    name = emailName[1];
            }
            planName = dsCreateUpdatePlanResponse.getSummary().getPlanName();
            brandId = dsCreateUpdatePlanResponse.getSummary().getBrandId();
        }

        if (updatedPlan != null) {
            for (DsCreateUpdateTacticResponse tacticResponse : dsCreateUpdatePlanResponse.getTactics()) {
                updatePredictionDataInChildPlan(tacticResponse, updatedPlan.getPlanId());
            }
            updatedPlan = updatePlanStatus(updatedPlan.getPlanId());
            if (updatedPlan != null)
                return new SavePredictionDataResponse("Prediction data saved successfully for plan id : " + updatedPlan.getPlanId() + ", plan name :" + planName + " and brand id :"
                        + brandId, email, name, environment.getProperty("env"));
        }
        return new SavePredictionDataResponse("Failed to save the prediction data for plan name :" + planName + " and brand id :"
                + brandId + ", parent plan id: " + parentPlanId, email, name, environment.getProperty("env"));
    }

    private AdvertiserPlanV2 updatePredictionDataInParentPlan(PlanSummaryResponse planSummaryResponse, long parentPlanId) {
        if (parentPlanId > 0 || (Objects.nonNull(planSummaryResponse.getPlanName()) && Objects.nonNull(planSummaryResponse.getBrandId()))) {
            try {
                AdvertiserPlanV2 updatedPlan;
                if (parentPlanId > 0) {
                    advertiserPlanV2Repository.updatePredictionDataInPlanByPlanId(planSummaryResponse, parentPlanId).toFuture().get();
                    updatedPlan = advertiserPlanV2Repository.findByPlanId(parentPlanId).toFuture().get();
                } else {
                    advertiserPlanV2Repository.updatePredictionDataInPlanByPlanNameAndBrandId(planSummaryResponse).toFuture().get();
                    updatedPlan = advertiserPlanV2Repository.findByPlanByBrandIdAndPlanName(planSummaryResponse.getPlanName(), planSummaryResponse.getBrandId()).toFuture().get();
                }
                return updatedPlan;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    private long getParentPlanId(DsCreateUpdatePlanResponse dsCreateUpdatePlanResponse) {
        long parentPlanId = 0;
        for (DsCreateUpdateTacticResponse tacticResponse : dsCreateUpdatePlanResponse.getTactics()) {
            if (parentPlanId == 0 && tacticResponse.getChildPlanId() > 0) {
                ChildAdvertiserPlan childAdvertiserPlan = plannerCache.getChildPlanByChildPlanId(tacticResponse.getChildPlanId());
                if (childAdvertiserPlan != null)
                    parentPlanId = childAdvertiserPlan.getParentPlanId();
            }
        }
        return parentPlanId;
    }

    private void updatePredictionDataInChildPlan(DsCreateUpdateTacticResponse tacticResponse, long parentPlanId) {
        if (Objects.nonNull(tacticResponse.getName()) && parentPlanId > 0) {
            try {
                boolean isChildPLanUpdated = childAdvertiserPlanRepository.updatePredictionDataInChildPlanByNameAndParentPlanId(tacticResponse, parentPlanId).toFuture().get() == 1;
                ChildAdvertiserPlan childAdvertiserPlan = childAdvertiserPlanRepository.findByChildPlanByNameAndParentPlanId(tacticResponse.getName(), parentPlanId).toFuture().get();
                if (isChildPLanUpdated) {
                    plannerCache.updateChildPlanV2Cache(childAdvertiserPlan);
                    plannerCache.reloadPlanWithIncludedChildPlansCache();
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw new DocereeApiException(e.getMessage());
            }
        }
    }

    private AdvertiserPlanV2 updatePlanStatus(long parentPlanId) {
        if (parentPlanId > 0) {
            try {
                boolean isPLanUpdated;
                AdvertiserPlanV2 updatedPlan;
                isPLanUpdated = advertiserPlanV2Repository.updatePlanStatusInPlanByPlanId(parentPlanId).toFuture().get() == 1;
                updatedPlan = advertiserPlanV2Repository.findByPlanId(parentPlanId).toFuture().get();
                if (isPLanUpdated) {
                    plannerCache.updatePlanV2Cache(updatedPlan);
                    plannerCache.reloadPlanWithIncludedChildPlansCache();
                }
                return updatedPlan;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }


    public AdvertiserPlanV2 getPlanByIdV2(long planId) throws DocereeApiException {
        try {
            if (planId > 0)
                return plannerCache.getPlanById(planId);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new DocereeApiException(e.getMessage());
        }
        return null;
    }


    public AdvertiserPlanV2 getPlanByPlanNameAndBrandId(String planName, String brandId) {
        try {
            return advertiserPlanV2Repository.findByPlanByBrandIdAndPlanName(planName, brandId).toFuture().get();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new DocereeApiException(e.getMessage());
        }
    }


    public ChildAdvertiserPlan getChildPlanByChildId(long planId) throws DocereeApiException {
        try {
            if (planId > 0)
                return plannerCache.getChildPlanByChildPlanId(planId);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new DocereeApiException(e.getMessage());
        }
        return new ChildAdvertiserPlan();
    }

    public ChildAdvertiserPlan getChildPlanByChildIdForDistGraph(long childPlanId) {
        try {
            if (childPlanId > 0) {
                return plannerCache.getChildPlanByChildPlanIdForDistGraph(childPlanId);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new DocereeApiException(e.getMessage());
        }
        return new ChildAdvertiserPlan();
    }

    public Set<String> getNdcCodesByBrandName(String brandName) {
        return plannerCache.getNdcCodesByBrandName(brandName);
    }

    public NpiListAndTaggedCountsByAudienceId getNpiListAndTaggedCountsByAudienceId(String _id) {
        try {
            NpiListAndTaggedCountsByAudienceId npiListAndTaggedCountsByAudienceId = new NpiListAndTaggedCountsByAudienceId();
            Set<Integer> npiSet = new HashSet<>();
            if (StringUtils.isNotBlank(_id)) {
                Arrays.stream(StringUtils.split(_id, ApplicationConstants.COMMA)).forEach(id -> {
                    if (StringUtils.isNotBlank(id)) {
                        id = id.split(ApplicationConstants.COLON)[0];
                        npiSet.addAll(plannerCache.getNpiByAudienceId(id));
                        AdvertiserAudienceTarget advertiserAudienceTarget = plannerCache.getAdvertiserAudienceTargetByAudienceId(id);
                        long totalTaggedCount = 0, pocTaggedCount = 0, endemicTaggedCount = 0, totalUploadedCount = 0;
                        if(advertiserAudienceTarget != null) {
                            if(advertiserAudienceTarget.getActiveTargetInfo() != null) {
                                totalUploadedCount = advertiserAudienceTarget.getActiveTargetInfo().getAudienceSize() != null ?
                                        advertiserAudienceTarget.getActiveTargetInfo().getAudienceSize() : 0;
                                totalTaggedCount = advertiserAudienceTarget.getActiveTargetInfo().getTagged() != null ?
                                        advertiserAudienceTarget.getActiveTargetInfo().getTagged() : 0;
                                pocTaggedCount = advertiserAudienceTarget.getActiveTargetInfo().getPoc() != null ?
                                        advertiserAudienceTarget.getActiveTargetInfo().getPoc() : 0;
                                endemicTaggedCount = advertiserAudienceTarget.getActiveTargetInfo().getEndemic() != null ?
                                        advertiserAudienceTarget.getActiveTargetInfo().getEndemic() : 0;
                            }
                        }
                        npiListAndTaggedCountsByAudienceId.setTotalUploadedCount(npiListAndTaggedCountsByAudienceId.getTotalUploadedCount() + totalUploadedCount);
                        npiListAndTaggedCountsByAudienceId.setTotalTaggedCount(npiListAndTaggedCountsByAudienceId.getTotalTaggedCount() + totalTaggedCount);
                        npiListAndTaggedCountsByAudienceId.setPocTaggedCount(npiListAndTaggedCountsByAudienceId.getPocTaggedCount() + pocTaggedCount);
                        npiListAndTaggedCountsByAudienceId.setEndTaggedCount(npiListAndTaggedCountsByAudienceId.getEndTaggedCount() + endemicTaggedCount);
                    }
                });
                npiListAndTaggedCountsByAudienceId.setNpiList(npiSet);
                return npiListAndTaggedCountsByAudienceId;
            }
        } catch (Exception e) {
            logger.error("Exception occurred while fetching npi ids and Audience tagged data by audience id. message : " + e.getMessage(), e);
        }
        return null;
    }

    public List<AdvertiserPlanV2> getAllActivePlans(String brandId, String advertiserId) {
        return plannerCache.getAllActivePlans(brandId, advertiserId);
    }

}
