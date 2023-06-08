package com.doceree.foresightService.models;

import lombok.*;

import java.util.Set;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class DiagnosisIntentSimilar {
    Set<AdvertiserAccount> accounts;


}
