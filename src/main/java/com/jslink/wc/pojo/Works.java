package com.jslink.wc.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jslink.wc.util.Constants;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "works")
@Entity
public class Works {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    private String title;
    @Column
    private String poster;
    @Column
    private String phone;
    @Column
    private Integer type;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;
    @Column
    private Byte status = Constants.WORKS_STATUS_DRAFT;
    @Column
    private String intro;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date mediaPlayDate;
    @Column
    private Integer mediaPlayTimes;
    @Column
    private String mediaLink;
    @Column
    private String mediaName;
    @Column
    private String subMediaName;
    @Column
    private String fileUrl;
    @Column
    private String reccFormFileUrl;//推荐表保存的位置
    @Column
    private String selfRecommendation;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    @Column
    private String projectBrief;
    @Column
    private String projectDesc;
    @Column
    private String vendor;

}
