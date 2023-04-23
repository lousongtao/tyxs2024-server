package com.jslink.wc.repository;

import com.jslink.wc.pojo.BrandPrize;
import com.jslink.wc.pojo.WorksPrize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface WorksPrizeRepository extends JpaRepository<WorksPrize, Integer>, JpaSpecificationExecutor<WorksPrize> {
    void deleteByWorksId(Integer id);

    List<WorksPrize> findByWorksId(Integer worksId);
}
