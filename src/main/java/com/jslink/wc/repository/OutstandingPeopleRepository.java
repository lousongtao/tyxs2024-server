package com.jslink.wc.repository;

import com.jslink.wc.pojo.OutstandingPeople;
import com.jslink.wc.pojo.PopsciMgmt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface OutstandingPeopleRepository extends JpaRepository<OutstandingPeople, Integer>, JpaSpecificationExecutor<OutstandingPeople> {
    List<OutstandingPeople> findByStatus(byte status);
}
