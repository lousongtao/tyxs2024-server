package com.jslink.wc.pojo;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "org_type")
@Entity
public class OrgType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    private String name;
    @Column
    private Integer accountQuantity;
}
