package com.jslink.wc.requestbody;

import lombok.Data;

import java.util.Date;

@Data
public class ExperienceBody {
    private Integer id;
    private Date startDate;
    private Date endDate;
    private String company;
    private String title;
}
