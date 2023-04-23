package com.jslink.wc.repository;

import com.jslink.wc.pojo.BrandSubsidize;
import com.jslink.wc.pojo.PeopleSubsidize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface PeopleSubsidizeRepository extends JpaRepository<PeopleSubsidize, Integer>, JpaSpecificationExecutor<PeopleSubsidize> {
    void deleteByPeopleId(Integer peopleId);

    List<PeopleSubsidize> findByPeopleId(Integer peopleId);
}
