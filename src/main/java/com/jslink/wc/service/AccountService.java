package com.jslink.wc.service;

import com.jslink.wc.pojo.Account;

import java.util.List;

public interface AccountService {
    List<Account> getAccounts();
    Account save(String user, Account account);

    Account login(String name, String password) throws Exception;

    Account getAccount(String name);
}
