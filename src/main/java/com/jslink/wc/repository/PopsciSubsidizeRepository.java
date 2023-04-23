package com.jslink.wc.repository;

import com.jslink.wc.pojo.BrandSubsidize;
import com.jslink.wc.pojo.PopsciSubsidize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface PopsciSubsidizeRepository extends JpaRepository<PopsciSubsidize, Integer>, JpaSpecificationExecutor<PopsciSubsidize> {
    void deleteByPopsciMgmtId(Integer popsciId);

    List<PopsciSubsidize> findByPopsciMgmtId(Integer popsciId);
}
