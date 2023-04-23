package com.jslink.wc.repository;

import com.jslink.wc.pojo.BrandPrize;
import com.jslink.wc.pojo.PopsciPrize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface PopsciPrizeRepository extends JpaRepository<PopsciPrize, Integer>, JpaSpecificationExecutor<PopsciPrize> {
    void deleteByPopsciMgmtId(Integer popsciId);

    List<PopsciPrize> findByPopsciMgmtId(Integer popsciId);
}
