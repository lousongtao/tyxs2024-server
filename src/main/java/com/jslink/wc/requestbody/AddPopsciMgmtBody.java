package com.jslink.wc.requestbody;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;

@Data
public class AddPopsciMgmtBody {
    private Integer applyType;
    private String deptName;
    private String deptAddress;
    private String deptContact;
    private String deptContactDept;
    private String deptMobile;
    private String deptEmail;
    private String name;
    private Integer gender;
    private String race;
    private Date birth;
    private Integer eduDegree;
    private String mobile;
    private String company;
    private String position;
    private String title;
    private String address;
    private String email;
    private String domain;
    private String projectBrief;
    private String projectDesc;
    private List<ExperienceBody> experienceList;
    private List<PrizeBody> prizeList;
    private List<SubsidizeBody> subsidizeList;
    private Byte status;
    private String fileUrl;
}
