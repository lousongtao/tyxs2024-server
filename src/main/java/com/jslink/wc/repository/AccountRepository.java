package com.jslink.wc.repository;

import com.jslink.wc.pojo.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Account findByAccount(String name);

    List<Account> findByParentAccountId(int id);
}
