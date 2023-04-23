package com.jslink.wc.controller;

import com.alibaba.fastjson.JSONObject;
import com.jslink.wc.pojo.Account;
import com.jslink.wc.responsebody.LoginBody;
import com.jslink.wc.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/wcapi/login")
public class LoginController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/outLogin")
    public Object outlogin(){
        JSONObject jo = new JSONObject();
        jo.put("success", true);
        return jo;
    }

    /**
     * 返回结果按照ant design pro要求的返回
     * @return
     */
    @PostMapping("/login")
    public LoginBody login(@RequestBody com.jslink.wc.responsebody.LoginBody body){
        Account account = null;
        try {
            account = accountService.login(body.getUsername(), body.getPassword());
            LoginBody lb = new LoginBody();
            if (account == null){
                lb.setStatus(LoginBody.STATUS_ERROR);
                return lb;
            }

            lb.setType("account");
            lb.setCurrentAuthority("admin");
            lb.setUsername(body.getUsername());
            lb.setPassword(body.getPassword());
            return lb;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}