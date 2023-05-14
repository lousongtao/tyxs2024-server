package com.jslink.wc.service;


import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.responsebody.StatsBody;

public interface StatsService {
    PageResult<StatsBody> getStats();

}
