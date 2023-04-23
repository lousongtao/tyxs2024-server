package com.jslink.wc.pojo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "account")
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    @Column
    private Byte type;
    @Column
    private String name;
    @Column
    private String account;
    @Column
    private String password;
    @Column
    private String phone;
//    @Column
//    private Integer areaId; //局方帐号, 上传用户, 这两类需要知道区域
    @Column
    private Integer parentAccountId; //上报账户要记录其所属的推荐账户
    @Column
    private Integer orgTypeId;
    @Column
    private String permission;
    @Column
    private String contactPerson;
    @Column
    private String email;
    @Column
    private int quantityBrand;
    @Column
    private int quantityPeople;
    @Column
    private int quantityMgmtIndividual;
    @Column
    private int quantityMgmtOrg;
    @Column
    private int quantityWorks;
}
