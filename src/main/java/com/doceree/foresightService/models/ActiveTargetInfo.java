package com.doceree.foresightService.models;

import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveTargetInfo {
    private Long audienceSize;
    private Long tagged;
    private Long poc;
    private Long endemic;
}
