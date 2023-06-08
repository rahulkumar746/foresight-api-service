package com.doceree.foresightService.models;


import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertiserAccountTarget {
    private String _id;
    private ActiveTargetInfoForAccount targetInfo;
}
