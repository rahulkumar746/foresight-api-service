package com.doceree.foresightService.models;

import lombok.*;

import java.util.List;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubCampaignMeta {

    private String usCpmBidValue;
    private boolean isTriggerBased;
    private List<String> businessRules;
}
