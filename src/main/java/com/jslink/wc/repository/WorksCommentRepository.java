package com.jslink.wc.repository;

import com.jslink.wc.pojo.WorksComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorksCommentRepository extends JpaRepository<WorksComment, Integer> {
//    List<WorksComment> findByWorksId(int workId);
}
