package com.jslink.wc.responsebody;

import lombok.Data;

@Data
public class AccountBody {
    private String name;
    private Integer userid;
    private String phone;
    private Byte type;
}
