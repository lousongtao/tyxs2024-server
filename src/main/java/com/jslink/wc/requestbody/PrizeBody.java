package com.jslink.wc.requestbody;

import lombok.Data;

import java.util.Date;

@Data
public class PrizeBody {
    private Integer seq;
    private Date date;
    private String prizeName;
    private String prizeLevel;
    private String prizeDept;
}
