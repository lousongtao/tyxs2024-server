package com.jslink.wc.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "works_comment")
@Entity
public class WorksComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
//    @Column
//    private String type;
//    @Column
//    private String value;
//    @Column
//    private String expertName;
//    @JsonIgnore
//    @ManyToOne
//    @JoinColumn(name = "works_id")
//    private Works works;
}
