package com.doceree.foresightService.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Slf4j
public class SavePredictionDataResponse {

    String message;
    String email;
    String name;
    String env;
}
