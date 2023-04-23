package com.jslink.wc.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "popsci_prize")
@Entity
public class PopsciPrize implements IPrize{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    private Integer seq;
    @Column
    @Temporal(TemporalType.DATE)
    private Date date;
    @Column
    private String prizeName;
    @Column
    private String prizeLevel;
    @Column
    private String prizeDept;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "popsci_id")
    private PopsciMgmt popsciMgmt;
}
