package com.jslink.wc.controller;

import com.jslink.wc.pojo.Account;
import com.jslink.wc.requestbody.UpdateAccountBody;
import com.jslink.wc.responsebody.*;
import com.jslink.wc.service.AccountService;
import com.jslink.wc.service.BaseController;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wcapi/account")
public class AccountController extends BaseController {
    @Autowired
    private AccountService accountService;

    /**
     * 根据登录人的权限去查找
     * @return
     */
    @GetMapping()
    public PageResult<Account> getAccounts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String account,
            @RequestParam(required = false) Integer orgTypeId,
            @RequestParam(required = false) Integer type,
            @RequestParam int current,
            @RequestParam int pageSize){
        Authentication authentication = authenticationFacade.getAuthentication();
        PageResult<Account> accounts = accountService.getAccounts(authentication.getName(), name, account, orgTypeId, type, current, pageSize);
        return accounts;
    }

    //根据作品查找推荐单位帐号
    @GetMapping("tjdw/works/{worksId}")
    public AccountBody getTjdwAccount(@PathVariable int worksId){
        AccountBody tjdw = accountService.getTjdwAccount(worksId);
        return tjdw;
    }

    @PostMapping()
    public Account addAccount(@RequestBody Account account){
        Authentication authentication = authenticationFacade.getAuthentication();
        return accountService.save(authentication.getName(), account);
    }

    @PutMapping()
    public Account updateAccount(@RequestBody UpdateAccountBody body){
        Authentication authentication = authenticationFacade.getAuthentication();
        return accountService.update(authentication.getName(), body);
    }

    @PutMapping("/password/{id}")
    public Account updateAccount( @PathVariable Integer id, @RequestParam String password){
        Authentication authentication = authenticationFacade.getAuthentication();
        return accountService.updatePassword(authentication.getName(), id, password);
    }

    /**
     * ant design pro每次login成功后, 需要再load一次user的信息
     * @return
     */
    @GetMapping("/currentuser")
    public CurrentUserBody currentUser(){
        Authentication authentication = authenticationFacade.getAuthentication();
        CurrentUserBody cb = new CurrentUserBody();
        cb.setSuccess(true);
        AccountBody ab = new AccountBody();
        Account account = accountService.getAccount(authentication.getName());
        BeanUtils.copyProperties(account, ab);
        ab.setUserid(account.getId());
        cb.setData(ab);
        return cb;
    }
}
