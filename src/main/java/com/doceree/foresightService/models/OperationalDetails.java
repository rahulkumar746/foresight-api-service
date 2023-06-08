package com.doceree.foresightService.models;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Set;


@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationalDetails {

    @BsonProperty
    private Set<String> bidSpecifications;
    @BsonProperty
    private Set<GlobalBidding> globalBidding;
}
