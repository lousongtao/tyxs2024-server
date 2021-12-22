package com.jslink.wc.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "works_attachment")
@Entity
public class WorksAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

//    @JsonIgnore
//    @ManyToOne
//    @JoinColumn(name = "works_id")
//    private Works works;
//
//    @Column
//    private String filePath;
//    @Column
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date postDate;
}
