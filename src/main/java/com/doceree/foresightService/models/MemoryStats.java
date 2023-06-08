package com.doceree.foresightService.models;

import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryStats {
    private long totalAllocatedMemory;//it can be initially less but can reach upto maxAllocated memory .Initially it is Xms.And it lies some where between Xms and Xmx.
    private long maxAllocatedMemory;//Xms=totalAllocatedMemory This much memory is assigned for jvm initially , but it does not mean it is using all the initial alloted memory  Xmx
    private long totalFreeMemore;//This is the actual memory occupied by java applications
    private long totalUsedMemory;
}
