package com.doceree.foresightService.models;

import lombok.*;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertiserAudience {
    @BsonProperty
    ObjectId _id;
    @BsonProperty
    ObjectId advertiserAudienceTargetId;
    @BsonProperty
    List<Integer> npi;
    @BsonProperty
    CreatedAt created;
    @BsonProperty
    Document modified;
}
