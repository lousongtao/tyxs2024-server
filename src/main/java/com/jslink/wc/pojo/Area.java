package com.jslink.wc.pojo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Table(name = "area")
@Entity
public class Area {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column
    private String name;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "area_id")
    private List<Street> streets;
}
