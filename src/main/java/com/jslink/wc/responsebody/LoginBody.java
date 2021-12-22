package com.jslink.wc.responsebody;

import lombok.Data;

//这个结构完全按照And Design Pro脚手架中设定的规则
@Data
public class LoginBody {
    public static final String STATUS_OK = "ok";
    public static final String STATUS_ERROR = "error";
    private String status = STATUS_OK;
    private String type;
    private String currentAuthority;

}
