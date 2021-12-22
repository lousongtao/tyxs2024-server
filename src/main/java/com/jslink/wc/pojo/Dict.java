package com.jslink.wc.pojo;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "dict")
@Entity
public class Dict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column
    private String type;
    @Column
    private Integer value;
    @Column
    private String name;
    @Column
    private String code;
}
