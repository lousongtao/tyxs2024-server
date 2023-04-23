package com.jslink.wc.repository;

import com.jslink.wc.pojo.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Integer>, JpaSpecificationExecutor {
    Account findByAccount(String name);

    List<Account> findByParentAccountId(Integer id);
}
