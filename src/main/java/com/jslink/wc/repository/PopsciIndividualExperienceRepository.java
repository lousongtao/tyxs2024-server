package com.jslink.wc.repository;

import com.jslink.wc.pojo.PeopleExperience;
import com.jslink.wc.pojo.PopsciIndividualExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface PopsciIndividualExperienceRepository extends JpaRepository<PopsciIndividualExperience, Integer>, JpaSpecificationExecutor<PopsciIndividualExperience> {
    void deleteByPopsciMgmtId(Integer popsciMgmtId);

    List<PopsciIndividualExperience> findByPopsciMgmtId(Integer popsciMgmtId);
}
