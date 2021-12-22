package com.jslink.wc.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "media")
@Entity
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date playDate;
    @Column
    private String name;
    @Column
    private String subName;
    @Column
    private String link;
    @Column
    private Integer length;
    @Column
    private Integer playTimes;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "works_id")
    private Works works;
}
