package com.jslink.wc.requestbody;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AddOutstandingPeopleBody {
    private Integer applyType;
    private String name;
    private Integer gender;
    private String race;
    private Date birth;
    private Integer eduDegree;
    private String phone;
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
