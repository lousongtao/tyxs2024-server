package com.jslink.wc.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name="popsci_individual_experience")
@Entity
public class PopsciIndividualExperience implements IExperience{
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
    @JoinColumn(name="popscimgmt_id")
    private PopsciMgmt popsciMgmt;
}
