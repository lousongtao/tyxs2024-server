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
    private int id;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    @Column
    private Byte type;
    @Column
    private Integer workerAccountQuantity;
    @Column
    private String name;
    @Column
    private String account;
    @Column
    private String password;
    @Column
    private String phone;
    @Column
    private Integer areaId; //局方帐号, 上传用户, 这两类需要知道区域
    @Column
    private Integer parentAccountId;
}
