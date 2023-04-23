package com.jslink.wc.requestbody;

import lombok.Data;


@Data
public class UpdateAccountBody {
    private Integer id;
    private Byte type;
    private String name;
    private String account;
    private String phone;
    private Integer orgTypeId;
    private String contactPerson;
    private String email;
    private String permission;
    private int quantityBrand;
    private int quantityPeople;
    private int quantityMgmtIndividual;
    private int quantityMgmtOrg;
    private int quantityWorks;
}
