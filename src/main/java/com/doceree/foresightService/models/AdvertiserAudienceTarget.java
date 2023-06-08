package com.doceree.foresightService.models;

import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertiserAudienceTarget {
    private String _id;
    private ActiveTargetInfo activeTargetInfo;
}