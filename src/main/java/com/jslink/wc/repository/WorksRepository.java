package com.jslink.wc.repository;

import com.jslink.wc.pojo.Works;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorksRepository extends JpaRepository<Works, Integer>, JpaSpecificationExecutor<Works> {
}
