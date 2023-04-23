package com.jslink.wc.repository;

import com.jslink.wc.pojo.PeopleExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface PeopleExperienceRepository extends JpaRepository<PeopleExperience, Integer>, JpaSpecificationExecutor<PeopleExperience> {
    void deleteByPeopleId(Integer peopleId);

    List<PeopleExperience> findByPeopleId(Integer peopleId);
}
