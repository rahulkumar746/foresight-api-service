package com.doceree.foresightService.service;

import com.doceree.foresightService.cache.PlannerCache;
import com.doceree.foresightService.repository.AdvertiserContextualTargetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@Configuration
public class CronAsyncService {
    private static final Logger logger = LoggerFactory.getLogger(CronAsyncService.class);

    @Autowired
    AdvertiserContextualTargetRepository advertiserContextualTargetRepository;
    @Autowired
    PlannerCache plannerCache;


    @Scheduled(cron = "0 55 23 * * *")
    public void reloadPlannerCaches() {
        long currentTimeInMillis = System.currentTimeMillis();
        logger.info("reloadPlannerCaches started");
        try {
            plannerCache.loadBrandNameToSetOfNdcs();
            advertiserContextualTargetRepository.loadSubCampaignDetailsMap();
        } catch (Exception e) {
            logger.error("reloadPlannerCaches failed.");
            return;
        }
        logger.info("reloadPlannerCaches finished. Time taken: " + (System.currentTimeMillis() - currentTimeInMillis));
    }
}
