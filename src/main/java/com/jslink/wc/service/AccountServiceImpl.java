package com.jslink.wc.service;

import com.jslink.wc.config.DataDict;
import com.jslink.wc.exception.ForbiddenException;
import com.jslink.wc.pojo.Account;
import com.jslink.wc.repository.AccountRepository;
import com.jslink.wc.util.Constants;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountServiceImpl implements AccountService{
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private DataDict dataDict;
    @Override
    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

    /**
     * 创建帐号, 前段不传递type, 根据操作人的权限, 按下面方式创建
     *          管理员能创建'推荐单位' 帐号;
     *          '管理员'帐号手工创建
     *          '推荐单位'能创建'申报单位';
     * @param userName
     * @param account
     * @return
     */
    @Override
    public Account save(String userName, Account account) {
        Account user = accountRepository.findByAccount(userName);
        if (user == null){
            throw new ForbiddenException();
        }
        if (user.getType() == Constants.ACCOUNT_TYPE_UNIT2) throw new ForbiddenException("无权限");
        if (user.getType() == Constants.ACCOUNT_TYPE_ADMIN){
            account.setType(Constants.ACCOUNT_TYPE_UNIT1);
        }
        if (user.getType() == Constants.ACCOUNT_TYPE_UNIT1){
            account.setType(Constants.ACCOUNT_TYPE_UNIT2);
        }
        //创建申报单位账号时, 要检查数量
        if (account.getType() == Constants.ACCOUNT_TYPE_UNIT2){
            int quantity = dataDict.getDicts().stream().filter(d -> d.getType().equals("account_quantity")).collect(Collectors.toList()).get(0).getValue();
            List<Account> accounts = accountRepository.findByParentAccountId(user.getId());
            if (accounts != null && accounts.size() >= quantity)
                throw new ForbiddenException("帐号达到最大数目");
        }
        if (account.getCreateDate() == null)
            account.setCreateDate(new Date());
        return accountRepository.save( account);
    }

    @Override
    public Account login(String name, String password) throws Exception {
        Account account = accountRepository.findByAccount(name);
        if (account == null || account.getPassword() == null || !account.getPassword().equals(password))
            throw new Exception("用户名密码不匹配");
        return account;
    }

    @Override
    public Account getAccount(String name) {
        return accountRepository.findByAccount(name);
    }
}
