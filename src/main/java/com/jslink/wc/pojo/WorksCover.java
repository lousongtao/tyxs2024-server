package com.jslink.wc.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "works_cover")
@Entity
public class WorksCover {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

//    @JsonIgnore
//    @OneToOne(mappedBy = "worksCover")
//    private Works works;
//
//    @Column
//    private String filePath;
//
//    @Column
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date postDate;
}
