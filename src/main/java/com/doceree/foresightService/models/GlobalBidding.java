package com.doceree.foresightService.models;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalBidding {

    @BsonProperty
    private String amount;
    @BsonProperty
    private String country;
}
