package com.jslink.wc.controller;

import com.alibaba.fastjson.JSONObject;
import com.jslink.wc.pojo.Account;
import com.jslink.wc.responsebody.AccountBody;
import com.jslink.wc.responsebody.BaseBody;
import com.jslink.wc.responsebody.CurrentUserBody;
import com.jslink.wc.responsebody.LoginBody;
import com.jslink.wc.service.AccountService;
import com.jslink.wc.service.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/wcapi/account")
public class AccountController extends BaseController {
    @Autowired
    private AccountService accountService;

    @GetMapping()
    public BaseBody<Account> getAccounts(){
        List<Account> accounts = accountService.getAccounts();
        BaseBody<Account> bb = new BaseBody<>();
        bb.setCurrent(1);
        bb.setData(accounts);
        bb.setTotal(accounts.size());
        bb.setPageSize(accounts.size());
        return bb;
    }

    @PostMapping()
    public Account addAccount(@RequestBody Account account){
        Authentication authentication = authenticationFacade.getAuthentication();
        return accountService.save("", account);
    }

    @PutMapping()
    public Account updateAccount(@RequestBody Account account){
        Authentication authentication = authenticationFacade.getAuthentication();
        return accountService.save("", account);
    }

    @PostMapping("/outLogin")
    public Object outlogin(){
        JSONObject jo = new JSONObject();
        jo.put("success", true);
        return jo;
    }

    /**
     * 返回结果按照ant design pro要求的返回
     * @param body
     * @return
     */
    @PostMapping("/login")
    public LoginBody login(@RequestBody LoginRequestBody body){
        Account account = null;
        try {
            account = accountService.login(body.username, body.password);
            LoginBody lb = new LoginBody();
            if (account == null){
                lb.setStatus(LoginBody.STATUS_ERROR);
                return lb;
            }

            lb.setType("account");
            lb.setCurrentAuthority("admin");

            return lb;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * ant design pro每次login成功后, 需要再load一次user的信息
     * @return
     */
    @GetMapping("/currentuser")
    public CurrentUserBody currentUser(){
        CurrentUserBody cb = new CurrentUserBody();
        cb.setSuccess(true);
        AccountBody ab = new AccountBody();
        Account account = accountService.getAccount("admin");
        ab.setName(account.getName());
        ab.setUserid(account.getId());
        ab.setPhone(account.getPhone());
        ab.setType(account.getType());
        cb.setData(ab);
        return cb;
    }

    static class LoginRequestBody{
        public String username;
        public String password;
    }
}
