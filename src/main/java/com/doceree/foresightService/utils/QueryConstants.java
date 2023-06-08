package com.doceree.foresightService.utils;

public final class QueryConstants {

    public static final String GET_ALL_BRAND_NDC_MAPPINGS = "select NDC_CODE, BRAND_NAME from BRAND_NDC_MAPPING";

    public static final String GET_CRAWLED_INFO_DATA = "SELECT URL,KEYWORDS FROM PAGEURL_CRAWLED_INFO WHERE URL IN (%s)";


    private QueryConstants() {

    }
}
