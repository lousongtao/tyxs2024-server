package com.jslink.wc.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name="people_subsidize")
@Entity
public class PeopleSubsidize implements ISubsidize{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    private Integer seq;
    @Column
    @Temporal(TemporalType.DATE)
    private Date date;
    @Column
    private String planNo;
    @Column
    private String planName;
    @Column
    private String planType;
    @Column
    private Double amount;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "people_id")
    private OutstandingPeople people;
}
