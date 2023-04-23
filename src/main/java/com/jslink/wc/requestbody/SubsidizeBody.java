package com.jslink.wc.requestbody;

import lombok.Data;

import java.util.Date;

@Data
public class SubsidizeBody {
    private Integer seq;
    private Date date;
    private String planNo;
    private String planName;
    private String planType;
    private Double amount;
}
