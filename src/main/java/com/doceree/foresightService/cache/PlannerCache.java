package com.doceree.foresightService.cache;

import com.doceree.foresightService.models.*;
import com.doceree.foresightService.repository.AdvertiserTargetRepository;
import com.doceree.foresightService.repository.AdvertiserPlanV2CrudRepository;
import com.doceree.foresightService.repository.ChildAdvertiserPlanRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.doceree.foresightService.utils.ApplicationConstants.BRAND_NAME;
import static com.doceree.foresightService.utils.ApplicationConstants.NDC_CODE;
import static com.doceree.foresightService.utils.QueryConstants.GET_ALL_BRAND_NDC_MAPPINGS;

@Component
public class PlannerCache {

    static final Logger logger = LoggerFactory.getLogger(PlannerCache.class);
    @Autowired
    ChildAdvertiserPlanRepository childAdvertiserPlanRepository;
    @Autowired
    AdvertiserPlanV2CrudRepository advertiserPlanV2CrudRepository;
    Map<Long, PlanWiseSubplan> planWiseSubplanMap = new HashMap<>();
    Map<Long, ChildAdvertiserPlan> childAdvertiserPlanCache = new ConcurrentHashMap<>();
    Set<String> uniquePlanNameAndBrandIdSet = ConcurrentHashMap.newKeySet();
    Map<Long, AdvertiserPlanV2> advertisePlanV2Cache = new ConcurrentHashMap<>();
    Map<Long, Set<String>> filteredNpiListOnChildPlan = new HashMap<>();
    Map<String,Set<String>> brandNameToSetOfNdcs = new HashMap<>();
    LoadingCache<String, AdvertiserAudience> audienceNpiCache;
    LoadingCache<String, AdvertiserAudienceTarget> advertiserAudienceTargetCache;

    LoadingCache<String, AdvertiserAccountTarget> advertiserAccountTargetCache;
    LoadingCache<String, AdvertiserIntentTarget> advertiserIntentTargetCache;
    @Autowired
    AdvertiserTargetRepository advertiserTargetRepository;
    ExecutorService executor;


    @Autowired
    @Qualifier("snowflakenAnalylticsJdbcTemplate")
    private JdbcTemplate snowflakeAnalyticsJdbcTempalte;


    @PostConstruct
    public void init() throws Exception {
        logger.info("Planner caches loading started.");
        long currentTimeInMillis = System.currentTimeMillis();
        executor = Executors.newFixedThreadPool(3);
        audienceNpiCache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(60)).expireAfterAccess(Duration.ofMinutes(30)).maximumSize(100).refreshAfterWrite(Duration.ofMinutes(1)).build(CacheLoader.asyncReloading(new CacheLoader<String, AdvertiserAudience>() {
            @Override
            public AdvertiserAudience load(String key) throws Exception {
                return advertiserTargetRepository.getAdvertiserAudience(key);
            }
        }, executor));
        advertiserAudienceTargetCache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(60)).expireAfterAccess(Duration.ofMinutes(30)).maximumSize(500).refreshAfterWrite(Duration.ofMinutes(1)).build(CacheLoader.asyncReloading(new CacheLoader<String, AdvertiserAudienceTarget>() {
            @Override
            public AdvertiserAudienceTarget load(String key) throws Exception {
                return advertiserTargetRepository.getAdvertiserAudienceTarget(key);
            }
        }, executor));
        advertiserAccountTargetCache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(60)).expireAfterAccess(Duration.ofMinutes(30)).maximumSize(500).refreshAfterWrite(Duration.ofMinutes(1)).build(CacheLoader.asyncReloading(new CacheLoader<String, AdvertiserAccountTarget>() {
            @Override
            public AdvertiserAccountTarget load(String key) throws Exception {
                return advertiserTargetRepository.getAdvertiserAccountTarget(key);
            }
        }, executor));
        advertiserIntentTargetCache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(60)).expireAfterAccess(Duration.ofMinutes(30)).maximumSize(500).refreshAfterWrite(Duration.ofMinutes(1)).build(CacheLoader.asyncReloading(new CacheLoader<String, AdvertiserIntentTarget>() {
            @Override
            public AdvertiserIntentTarget load(String key) throws Exception {
                return advertiserTargetRepository.getAdvertiserIntentTarget(key);
            }
        }, executor));
        loadAll();
        reloadPlanWithIncludedChildPlansCache();
        logger.info("All Planner caches loaded. time(ms) taken: " + (System.currentTimeMillis() - currentTimeInMillis));
    }

    public void loadAll() throws Exception {
        advertiserPlanV2CrudRepository.getAllActivePlans().collectList().toFuture().get().forEach(p -> {
            advertisePlanV2Cache.put(p.getPlanId(), p);
            uniquePlanNameAndBrandIdSet.add(getUniqueKeyForAdvertiserPlanV2(p));
        });
        logger.info("Parent Plan cache loaded.");
        childAdvertiserPlanRepository.findPlanByDeletedStatus(Boolean.FALSE).collectList().toFuture().get().forEach(p -> {
            childAdvertiserPlanCache.put(p.getChildPlanId(), p);
        });
        logger.info("Child Plan cache loaded.");

        loadBrandNameToSetOfNdcs();
        logger.info("Brand Id to Ndc codes cache loaded.");
    }
    public void loadBrandNameToSetOfNdcs() {
        Map<String,Set<String>> brandNameToSetOfNdcs = new HashMap<>();
        List<Map<String, Object>> maps = snowflakeAnalyticsJdbcTempalte.queryForList(GET_ALL_BRAND_NDC_MAPPINGS);
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                String brandName = (String) map.get(BRAND_NAME);
                String ndcCode = (String) map.get(NDC_CODE);
                if (StringUtils.isNotBlank(brandName) && StringUtils.isNotBlank(ndcCode)) {
                    brandNameToSetOfNdcs.putIfAbsent(brandName.toLowerCase(), new HashSet<>());
                    brandNameToSetOfNdcs.get(brandName.toLowerCase()).add(ndcCode);
                }
            }
        }
        this.brandNameToSetOfNdcs = brandNameToSetOfNdcs;
    }
    public PlanWiseSubplan getPlanWiseSubplanById(long planId) {
        return planWiseSubplanMap.get(planId);
    }

    public void reloadPlanWithIncludedChildPlansCache() {

        planWiseSubplanMap = getPlansWithIncludedChildPlans();
    }
    public Map<Long, PlanWiseSubplan> getPlansWithIncludedChildPlans() {
        Map<Long, PlanWiseSubplan> planWiseSubplanMap = new HashMap<>();
        try {
            Map<Long, AdvertiserPlanV2> allActivePlans = getAllplans();
            Set<ChildAdvertiserPlan> allActiveChildPlans = getAllActiveChildPlans();
            for (ChildAdvertiserPlan childAdvertiserPlan : allActiveChildPlans) {
                long parentPlanId = childAdvertiserPlan.getParentPlanId();
                planWiseSubplanMap.putIfAbsent(parentPlanId, new PlanWiseSubplan());
                PlanWiseSubplan planWiseSubplan = planWiseSubplanMap.get(parentPlanId);
                planWiseSubplan.setParentPlan(allActivePlans.get(parentPlanId));
                if(!childAdvertiserPlan.isDeleted())
                    planWiseSubplan.getChildPlans().add(childAdvertiserPlan);
                planWiseSubplan.getChildPlanMap().put(childAdvertiserPlan.getTacticName(), childAdvertiserPlan);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return planWiseSubplanMap;
    }
    public Map<Long, AdvertiserPlanV2> getAllplans() throws Exception {
        Map<Long, AdvertiserPlanV2> parentPlans = new HashMap<>();
        Collection<AdvertiserPlanV2> values = advertisePlanV2Cache.values();
        for (AdvertiserPlanV2 advertiserPlanV2 : values) {
            parentPlans.putIfAbsent(advertiserPlanV2.getPlanId(), advertiserPlanV2);
        }
        return parentPlans;
    }

    public Set<ChildAdvertiserPlan> getAllActiveChildPlans() throws Exception {
        Collection<ChildAdvertiserPlan> values = childAdvertiserPlanCache.values();
        Set<ChildAdvertiserPlan> planSet = new HashSet<>(values);
        return planSet;
    }

    private static String getUniqueKeyForAdvertiserPlanV2(AdvertiserPlanV2 advertiserPlanV2) {
        return advertiserPlanV2.getPlanName().toLowerCase() + "_" + advertiserPlanV2.getBrandId().toLowerCase();
    }

    public AdvertiserPlanV2 getPlanById(long planId) {
        try {
            return Optional.ofNullable(advertisePlanV2Cache.get(planId)).orElse(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    public void updatePlanV2Cache(AdvertiserPlanV2 advertiserPlanV2) {
        try {
            advertisePlanV2Cache.put(advertiserPlanV2.getPlanId(), advertiserPlanV2);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void removeExistingAndAddUpdatedPlanInUniquePlanNameAndBrandIdCache(AdvertiserPlanV2 existingPlan, AdvertiserPlanV2 updatedPlan) {
        String uniqueKeyForExistingAdvertiserPlanV2 = existingPlan != null ? getUniqueKeyForAdvertiserPlanV2(existingPlan) : null;
        if(uniqueKeyForExistingAdvertiserPlanV2 != null && uniquePlanNameAndBrandIdSet.contains(uniqueKeyForExistingAdvertiserPlanV2))
            uniquePlanNameAndBrandIdSet.remove(uniqueKeyForExistingAdvertiserPlanV2);
        uniquePlanNameAndBrandIdSet.add(getUniqueKeyForAdvertiserPlanV2(updatedPlan));
    }
    public boolean canBeProcessed(AdvertiserPlanV2 advertiserPlanV2) {
        AdvertiserPlanV2 existingPlan;
        String uniqueKeyForExistingPlan = null;
        String uniqueKeyForNewPlan = getUniqueKeyForAdvertiserPlanV2(advertiserPlanV2);
        if(advertiserPlanV2.getPlanId() > 0) {
            existingPlan = advertisePlanV2Cache.containsKey(advertiserPlanV2.getPlanId()) ? advertisePlanV2Cache.get(advertiserPlanV2.getPlanId()) : null;
            uniqueKeyForExistingPlan = existingPlan != null ? getUniqueKeyForAdvertiserPlanV2(existingPlan) : null;
        }
        return uniqueKeyForNewPlan.equals(uniqueKeyForExistingPlan) || !uniquePlanNameAndBrandIdSet.contains(uniqueKeyForNewPlan);
    }
    public void addPlanInUniquePlanNameAndBrandIdCache(AdvertiserPlanV2 advertiserPlanV2) {
        uniquePlanNameAndBrandIdSet.add(getUniqueKeyForAdvertiserPlanV2(advertiserPlanV2));
    }
    public void updateChildPlanV2Cache(ChildAdvertiserPlan childAdvertiserPlan) {
        try {
            childAdvertiserPlanCache.put(childAdvertiserPlan.getChildPlanId(), childAdvertiserPlan);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    public void updateFilteredNpiListOnChildPlan(ChildAdvertiserPlan childAdvertiserPlan, Set<String> filteredNpiList) {
        filteredNpiListOnChildPlan.put(childAdvertiserPlan.getChildPlanId(), filteredNpiList);
    }


    public ChildAdvertiserPlan getChildPlanByChildPlanId(long planId) {
        try {
            return childAdvertiserPlanCache.get(planId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ChildAdvertiserPlan();
    }

    public ChildAdvertiserPlan getChildPlanByChildPlanIdForDistGraph(long childPlanId) {
        try {
            ChildAdvertiserPlan responseChildPlan = childAdvertiserPlanCache.get(childPlanId);
            if(responseChildPlan != null && filteredNpiListOnChildPlan.containsKey(childPlanId) &&
                    advertisePlanV2Cache.containsKey(responseChildPlan.getParentPlanId()) &&
                    advertisePlanV2Cache.get(responseChildPlan.getParentPlanId()).getStatus() == 1) {
                responseChildPlan.setNpiList(filteredNpiListOnChildPlan.get(childPlanId));
                filteredNpiListOnChildPlan.remove(childPlanId);
            }
            return responseChildPlan;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ChildAdvertiserPlan();
    }

    public Set<String> getNdcCodesByBrandName(String brandName) {
        return brandNameToSetOfNdcs.get(brandName.toLowerCase());
    }
    public List<Integer> getNpiByAudienceId(String k) {
        try {
            return Optional.ofNullable(Optional.ofNullable(audienceNpiCache.get(k)).orElse(new AdvertiserAudience()).getNpi()).orElse(new ArrayList<>());
        } catch (Exception e) {
            logger.error("Exception occurred while fetching npi list from mongo on audience id : " + k + ", reason :" + e.getMessage());
        }
        return new ArrayList<>();
    }


    public AdvertiserAccountTarget getAdvertiserAccountTargetByAccountId(String k) {
        try {
            return Optional.ofNullable(Optional.ofNullable(advertiserAccountTargetCache.get(k)).orElse(new AdvertiserAccountTarget())).orElse(new AdvertiserAccountTarget());
        } catch (Exception e) {
            logger.error("Exception occurred while fetching account targeting data from mongoDb on account id : " + k + ", reason :" + e.getMessage());
        }
        return new AdvertiserAccountTarget();
    }

    public AdvertiserIntentTarget getAdvertiserIntentTargetByIntentId(String k) {
        try {
            return Optional.ofNullable(Optional.ofNullable(advertiserIntentTargetCache.get(k)).orElse(new AdvertiserIntentTarget())).orElse(new AdvertiserIntentTarget());
        } catch (Exception e) {
            logger.error("Exception occurred while fetching intent targeting data from mongoDb on intent id : " + k + ", reason :" + e.getMessage());
        }
        return new AdvertiserIntentTarget();
    }
    public AdvertiserAudienceTarget getAdvertiserAudienceTargetByAudienceId(String k) {
        try {
            return Optional.ofNullable(Optional.ofNullable(advertiserAudienceTargetCache.get(k)).orElse(new AdvertiserAudienceTarget())).orElse(new AdvertiserAudienceTarget());
        } catch (Exception e) {
            logger.error("Exception occurred while fetching tagged count data from mongo on audience id : " + k + ", reason :" + e.getMessage());
        }
        return new AdvertiserAudienceTarget();
    }

    public List<AdvertiserPlanV2> getAllActivePlans(String brandId, String advertiserId) {
        List<AdvertiserPlanV2> planSet = new ArrayList<>();
        List<AdvertiserPlanV2> values;
        try {
            Collection<AdvertiserPlanV2> collection = advertisePlanV2Cache.values();
            if (collection == null || collection.size() == 0) {
                logger.info("Empty plan cache found. Reloading it from db.");
                init();
                collection = advertisePlanV2Cache.values();
                values = new ArrayList<>(collection);
            } else
                values = new ArrayList<>(collection);
            Collections.sort(values, new Comparator<AdvertiserPlanV2>() {
                public int compare(AdvertiserPlanV2 o1, AdvertiserPlanV2 o2) {
                    if (o1 == null || o2 == null || o1.getUpdatedAt() == null || o2.getUpdatedAt() == null) {
                        logger.error("Found null plan object. o1:" + o1.toString() + ", o2:" + o2.toString());
                        return 0;
                    }
                    return ((o1.getUpdatedAt().before(o2.getUpdatedAt())) ? 1 : (o1.getUpdatedAt().after(o2.getUpdatedAt()) ? -1 : 0));
                }
            });
        } catch (Exception e) {
            logger.error(" Exception occurred while creating plan list. cache size : " + advertisePlanV2Cache.size() + ", message : ", e);
            return planSet;
        }
        if (values == null || values.size() == 0) {
            logger.error("Couldn't fetch Plan list from db. Either null or empty.");
            return planSet;
        }
        for (AdvertiserPlanV2 advertiserPlanV2 : values) {
            if (StringUtils.isNotBlank(advertiserPlanV2.getBrandId()) && advertiserPlanV2.getBrandId().equalsIgnoreCase(brandId)
                    && StringUtils.isNotBlank(advertiserPlanV2.getAdvertiserId()) && advertiserPlanV2.getAdvertiserId().equalsIgnoreCase(advertiserId)) {
                planSet.add(advertiserPlanV2);
            }
        }
        return planSet;
    }

}
