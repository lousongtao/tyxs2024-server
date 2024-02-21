package com.jslink.wc.repository;

import com.jslink.wc.pojo.ReturnHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ReturnHistoryRepository extends JpaRepository<ReturnHistory, Integer>, JpaSpecificationExecutor {
    List<ReturnHistory> findByObjectIdAndType(Integer objectId, int type);
}
