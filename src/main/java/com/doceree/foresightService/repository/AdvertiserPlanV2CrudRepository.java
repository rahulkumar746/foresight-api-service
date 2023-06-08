package com.doceree.foresightService.repository;

import com.doceree.foresightService.models.AdvertiserPlanV2;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AdvertiserPlanV2CrudRepository  extends ReactiveCrudRepository<AdvertiserPlanV2,Long> {

    @Query("select * from advertiser_plan where deleted=false  order by updated_at desc")
    public Flux<AdvertiserPlanV2> getAllActivePlans();

}
