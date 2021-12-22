package com.jslink.wc.pojo;

import com.jslink.wc.util.Constants;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Table(name = "works")
@Entity
public class Works {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    @Column
    private String poster;
    @Column
    private String title;
//    @Column
//    private Integer street;
    @Column
    private Byte status = Constants.WORKS_STATUS_NOAUDIT;
    @Column
    private String intro;
    @Column
    private String phone;
    @Column
    private String type;
//    @OneToOne
//    @JoinColumn(name = "workscover_id", referencedColumnName = "id")
//    private WorksCover worksCover;
//    @OneToMany
//    @JoinColumn(name = "works_id", referencedColumnName = "id")
//    private List<WorksAttachment> worksAttachments;
//    @OneToMany
//    @JoinColumn(name = "works_id")
//    private List<WorksComment> worksComments;
    @OneToMany
    @JoinColumn(name = "works_id")
    private List<Media> medias;
}
