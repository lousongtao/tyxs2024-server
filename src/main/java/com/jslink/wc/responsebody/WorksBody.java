package com.jslink.wc.responsebody;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jslink.wc.pojo.ReturnHistory;
import com.jslink.wc.pojo.WorksPrize;
import com.jslink.wc.pojo.WorksSubsidize;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WorksBody {
    private Integer id;
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date createDate;
    private String poster;
    private String title;
    private Byte status;
    private String intro;
    private String phone;
    private Integer type;
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date mediaPlayDate;
    private Integer mediaPlayTimes;
    private String mediaLink;
    private String mediaName;
    private String subMediaName;
    private String fileUrl;
    private String reccFormFileUrl;//推荐表保存的位置
    private String projectBrief;
    private String projectDesc;
    private String vendor;
    private String selfRecommendation;
    private List<WorksPrize> prizeList;
    private List<WorksSubsidize> subsidizeList;
    private Integer accountId;
    private ReturnHistory returnHistory;
    private String topic;
}
