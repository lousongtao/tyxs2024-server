package com.jslink.wc.repository;

import com.jslink.wc.pojo.BrandPrize;
import com.jslink.wc.pojo.PeopleExperience;
import com.jslink.wc.pojo.PeoplePrize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface PeoplePrizeRepository extends JpaRepository<PeoplePrize, Integer>, JpaSpecificationExecutor<PeoplePrize> {
    void deleteByPeopleId(Integer peopleId);

    List<PeoplePrize> findByPeopleId(Integer PeopleId);
}
