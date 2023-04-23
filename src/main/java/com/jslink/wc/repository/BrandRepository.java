package com.jslink.wc.repository;

import com.jslink.wc.pojo.Brand;
import com.jslink.wc.pojo.OutstandingPeople;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface BrandRepository extends JpaRepository<Brand, Integer>, JpaSpecificationExecutor<Brand> {
    List<Brand> findByStatus(byte status);
}
