package com.doceree.foresightService.models;

import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertiserIntentTarget {

    private String _id;
    private ActiveTargetInfoForIntent targetInfo;
}
