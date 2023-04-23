package com.jslink.wc.repository;

import com.jslink.wc.pojo.BrandSubsidize;
import com.jslink.wc.pojo.PeopleSubsidize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface BrandSubsidizeRepository extends JpaRepository<BrandSubsidize, Integer>, JpaSpecificationExecutor<BrandSubsidize> {
    void deleteByBrandId(Integer brandId);

    List<BrandSubsidize> findByBrandId(Integer brandId);
}
