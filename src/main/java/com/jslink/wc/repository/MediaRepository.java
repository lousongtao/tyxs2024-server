package com.jslink.wc.repository;

import com.jslink.wc.pojo.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Integer> {
    List<Media> findByWorksId(int worksId);
}
