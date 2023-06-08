package com.doceree.foresightService.models;

import lombok.*;

import java.util.Set;

@Data
@ToString
@Builder

public class ActiveTargetInfoForAccount {
    Set<AdvertiserAccount> accounts;
}
