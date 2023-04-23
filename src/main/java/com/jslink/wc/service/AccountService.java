package com.jslink.wc.service;

import com.jslink.wc.pojo.Account;
import com.jslink.wc.requestbody.UpdateAccountBody;
import com.jslink.wc.responsebody.AccountBody;
import com.jslink.wc.responsebody.PageResult;

import java.util.List;

public interface AccountService {
    PageResult<Account> getAccounts(String user, String name, String account, Integer orgTypeId, Integer type, int page, int pageSize);
    Account save(String user, Account account);

    Account update(String user, UpdateAccountBody body);

    Account login(String name, String password) throws Exception;

    Account getAccount(String name);

    Account updatePassword(String user, Integer id, String password);

    AccountBody getTjdwAccount(int worksId);
}
