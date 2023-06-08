package com.doceree.foresightService.models;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.Set;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertiserSubCampaign {

    @BsonProperty
    private ObjectId _id;
    @BsonProperty
    private OperationalDetails operationalDetails;
    @BsonProperty
    private Set<String> networks;
    @BsonProperty
    private Object selectedPocBusinessRules;
}
