package com.jslink.wc.repository;

import com.jslink.wc.pojo.BrandSubsidize;
import com.jslink.wc.pojo.WorksSubsidize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface WorksSubsidizeRepository extends JpaRepository<WorksSubsidize, Integer>, JpaSpecificationExecutor<WorksSubsidize> {
    void deleteByWorksId(Integer id);

    List<WorksSubsidize> findByWorksId(Integer worksId);
}
