package com.doceree.foresightService.repository;

import com.doceree.foresightService.models.AdvertiserAccountTarget;
import com.doceree.foresightService.models.AdvertiserAudience;
import com.doceree.foresightService.models.AdvertiserAudienceTarget;
import com.doceree.foresightService.models.AdvertiserIntentTarget;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class AdvertiserTargetRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    public AdvertiserAudience getAdvertiserAudience(String _id) {
        return mongoTemplate.findOne(Query.query(Criteria.where("advertiserAudienceTargetId").is(new ObjectId(_id))), AdvertiserAudience.class, "advertiser_audiences");
    }

    public AdvertiserAudienceTarget getAdvertiserAudienceTarget(String _id) {
        return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(new ObjectId(_id))), AdvertiserAudienceTarget.class, "advertiser_audience_targets");
    }

    public AdvertiserAccountTarget getAdvertiserAccountTarget(String _id) {
        return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(new ObjectId(_id))), AdvertiserAccountTarget.class, "advertiser_account_target");
    }

    public AdvertiserIntentTarget getAdvertiserIntentTarget(String _id) {
        return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(new ObjectId(_id))), AdvertiserIntentTarget.class, "advertiser_intent_target");
    }

}
