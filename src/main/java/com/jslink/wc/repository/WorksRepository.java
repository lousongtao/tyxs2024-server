package com.jslink.wc.repository;

import com.jslink.wc.pojo.Works;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface WorksRepository extends JpaRepository<Works, Integer>, JpaSpecificationExecutor<Works> {
    List<Works> findByFileUrl(String fileUrl);
}
