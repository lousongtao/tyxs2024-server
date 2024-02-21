package com.jslink.wc.requestbody;

import com.jslink.wc.util.Constants;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AddWorkBody {
    private Integer id;
    private String title;
    private String poster;
    private String phone;
    private Integer type;
    private Date createDate;
    private Byte status;
    private String intro;
    private Date mediaPlayDate;
    private Integer mediaPlayTimes;
    private String mediaLink;
    private String mediaName;
    private String subMediaName;
    private String fileUrl;
    private String vendor;
    private String projectBrief;
    private String projectDesc;
    private String selfRecommendation;
    private List<PrizeBody> prizeList;
    private List<SubsidizeBody> subsidizeList;
}
