package com.jslink.wc.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jslink.wc.util.Constants;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table
@Entity
public class OutstandingPeople {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    private Integer applyType;
    @Column
    private String name;
    @Column
    private Integer gender;
    @Column
    private String race;
    @Column
    @Temporal(TemporalType.DATE)
    private Date birth;
    @Column
    private Integer eduDegree;
    @Column
    private String phone;
    @Column
    private String company;
    @Column
    private String position;
    @Column
    private String title;
    @Column
    private String address;
    @Column
    private String email;
    @Column
    private String domain;
    @Column
    private String fileUrl;
    @Column
    private String reccFormFileUrl;//推荐表保存的位置
    @Column
    private String projectBrief;
    @Column
    private String projectDesc;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    @Column
    private Byte status = Constants.WORKS_STATUS_DRAFT;
}
