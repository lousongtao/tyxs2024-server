package com.jslink.wc.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name="people_experience")
@Entity
public class PeopleExperience implements IExperience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    @Temporal(TemporalType.DATE)
    private Date startDate;
    @Column
    @Temporal(TemporalType.DATE)
    private Date endDate;
    @Column
    private String company;
    @Column
    private String title;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="people_id")
    private OutstandingPeople people;


}
