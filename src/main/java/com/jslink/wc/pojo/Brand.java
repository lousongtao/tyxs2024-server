package com.jslink.wc.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jslink.wc.util.Constants;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table
@Entity
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    private String name;
    @Column
    private String type;
    @Column
    private String category;
    @Column
    private String company;
    @Column
    private String contactPerson;
    @Column
    private String phone;
    @Column
    private String email;
    @Column
    private String address;
    @Column
    private String projectBrief;
    @Column
    private String projectDesc;
    @Column
    private String fileUrl;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    @Column
    private Byte status = Constants.WORKS_STATUS_DRAFT;
    @Column
    private String reccFormFileUrl;//推荐表保存的位置
}
