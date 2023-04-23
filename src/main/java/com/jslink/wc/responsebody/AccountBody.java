package com.jslink.wc.responsebody;

import lombok.Data;

import javax.persistence.Column;


@Data
public class AccountBody {
    private String name;
    private Integer userid;
    private String phone;
    private Byte type;
    private Integer orgTypeId;
    private String orgTypeName;
    private String permission;
    private String contactPerson;
    private String email;

}
