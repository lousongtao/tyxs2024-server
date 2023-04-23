package com.jslink.wc.repository;

import com.jslink.wc.pojo.BrandPrize;
import com.jslink.wc.pojo.PeoplePrize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface BrandPrizeRepository extends JpaRepository<BrandPrize, Integer>, JpaSpecificationExecutor<BrandPrize> {
    void deleteByBrandId(Integer brandId);

    List<BrandPrize> findByBrandId(Integer brandId);
}
