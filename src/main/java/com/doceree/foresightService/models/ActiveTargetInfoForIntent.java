package com.doceree.foresightService.models;

import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveTargetInfoForIntent {
    private SearchIntents searchIntents;
    private RxIntentCompetitor rxIntentCompetitor;
    private DiagnosisIntentSimilar diagnosisIntentSimilar;
}
