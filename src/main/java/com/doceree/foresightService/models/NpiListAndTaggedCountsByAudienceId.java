package com.doceree.foresightService.models;


import lombok.*;

import java.util.Set;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NpiListAndTaggedCountsByAudienceId {

    Set<Integer> npiList;
    long totalUploadedCount;
    long totalTaggedCount;
    long pocTaggedCount;
    long endTaggedCount;
}
