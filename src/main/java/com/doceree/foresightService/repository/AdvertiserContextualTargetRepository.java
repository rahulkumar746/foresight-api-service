package com.doceree.foresightService.repository;

import com.doceree.foresightService.models.AdvertiserSubCampaign;
import com.doceree.foresightService.models.GlobalBidding;
import com.doceree.foresightService.models.SubCampaignMeta;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.doceree.foresightService.utils.ApplicationConstants.*;

@Repository
public class AdvertiserContextualTargetRepository {

    private static final Logger logger = LoggerFactory.getLogger(AdvertiserContextualTargetRepository.class);

    private Map<String, SubCampaignMeta> subCampaignMetaMap = new HashMap<>();

    @Autowired
    MongoTemplate mongoTemplate;


    public String loadSubCampaignDetailsMap() {
        Map<String, SubCampaignMeta> subCampaignMetaMap = new HashMap<>();
        logger.info("Loading of subCampaignMeta Cache started.");
        String message = "";
        long currentMillis = System.currentTimeMillis();
        try {
            List<AdvertiserSubCampaign> subCampaignList = mongoTemplate.findAll(AdvertiserSubCampaign.class, ADVERTISER_SUB_CAMPAIGNS);
            if (subCampaignList != null) {
                for (AdvertiserSubCampaign subCampaign : subCampaignList) {
                    SubCampaignMeta subCampaignMeta = new SubCampaignMeta();
                    String subCampaignId = String.valueOf(subCampaign.get_id());

                    setUsCpmBidValue(subCampaign, subCampaignMeta);
                    setPocRulesMeta(subCampaign, subCampaignMeta, subCampaignId);

                    subCampaignMetaMap.put(subCampaignId, subCampaignMeta);
                }
            }
            message = "Loading of subCampaignMeta Cache ended. time(ms) taken : " + (System.currentTimeMillis() - currentMillis);
            logger.info(message);
        } catch (Exception e) {
            message = "Exception occurred while Loading of subCampaignMeta Cache. Reason :";
            logger.error(message, e);
        }
        if (subCampaignMetaMap.size() > 0) {
            this.subCampaignMetaMap = subCampaignMetaMap;
        }
        return message;
    }

    private static void setUsCpmBidValue(AdvertiserSubCampaign subCampaign, SubCampaignMeta subCampaignMeta) {
        Set<String> bidSpecifications = subCampaign.getOperationalDetails() != null ?
                subCampaign.getOperationalDetails().getBidSpecifications() : null;
        Set<GlobalBidding> globalBiddings = subCampaign.getOperationalDetails() != null ?
                subCampaign.getOperationalDetails().getGlobalBidding() : null;
        if (bidSpecifications != null && globalBiddings != null
                && bidSpecifications.stream().anyMatch(s -> CPM.equalsIgnoreCase(s))) {
            for (GlobalBidding globalBidding : globalBiddings) {
                if (globalBidding.getCountry() != null && US.equalsIgnoreCase(globalBidding.getCountry())) {
                    subCampaignMeta.setUsCpmBidValue(globalBidding.getAmount());
                }
            }
        }
    }

    private static void setPocRulesMeta(AdvertiserSubCampaign subCampaign, SubCampaignMeta subCampaignMeta, String subCampaignId) {
        List<String> businessRules = null;
        LinkedHashMap<String, Set<LinkedHashMap<String, List<String>>>> businessRuleMap = null;
        if (subCampaign.getNetworks() != null
                && subCampaign.getNetworks().stream().anyMatch(s -> POC.equalsIgnoreCase(s))
                && subCampaign.getSelectedPocBusinessRules() != null) {
            boolean isValidPocBusinessRule = false;
            try {
                businessRules = (List<String>) subCampaign.getSelectedPocBusinessRules();
            } catch (Exception e) {
                try {
                    businessRuleMap = (LinkedHashMap<String, Set<LinkedHashMap<String, List<String>>>>) subCampaign.getSelectedPocBusinessRules();
                } catch (Exception ex) {
                    logger.error("Can not parse sub campaign meta, sub campaign id : ." + subCampaignId, ex);
                }
            }
            if (businessRules != null) {
                for (String businessRule : businessRules) {
                    if (StringUtils.isNotBlank(businessRule)) {
                        isValidPocBusinessRule = true;
                        break;
                    }
                }
            } else if (businessRuleMap != null) {
                List<LinkedHashMap<String, List<String>>> businessRule = (List<LinkedHashMap<String, List<String>>>) businessRuleMap.get(BUSINESS_RULES);
                if (businessRule != null) {
                    for (LinkedHashMap<String, List<String>> map : businessRule) {
                        if (isValidPocBusinessRule)
                            break;
                        List<String> rules = map.get(RULES);
                        if (rules != null) {
                            for (String rule : rules) {
                                if (StringUtils.isNotBlank(rule)) {
                                    if (!isValidPocBusinessRule) {
                                        isValidPocBusinessRule = true;
                                        businessRules = new ArrayList<>();
                                    }
                                    businessRules.add(rule);
                                }
                            }
                        }
                    }
                }
            }
            subCampaignMeta.setBusinessRules(businessRules);
            subCampaignMeta.setTriggerBased(isValidPocBusinessRule);
        }
    }

}
