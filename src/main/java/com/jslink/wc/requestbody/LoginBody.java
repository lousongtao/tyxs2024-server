package com.jslink.wc.requestbody;

import lombok.Data;

@Data
public class LoginBody {
    private String username;
    private String password;
    private String type;
}
