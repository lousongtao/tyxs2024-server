package com.jslink.wc.repository;

import com.jslink.wc.pojo.SystemTurnOffTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SystemTurnOffTimeRepository extends JpaRepository<SystemTurnOffTime, Integer>, JpaSpecificationExecutor {
}
