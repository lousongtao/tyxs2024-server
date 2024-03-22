package com.jslink.wc.pojo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "system_turnoff_time")
@Entity
public class SystemTurnOffTime {
    @Column
    private Date time;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
}
