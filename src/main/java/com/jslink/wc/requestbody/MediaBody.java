package com.jslink.wc.requestbody;

import lombok.Data;

import java.util.Date;

@Data
public class MediaBody {
    private int id;
    private String name;  //栏目
    private String subName; //子栏目
    private Date playDate; //播放日期
    private String link; //作品链接
    private Integer length;//播放时长, min
    private Integer playTimes;//播放次数
}
