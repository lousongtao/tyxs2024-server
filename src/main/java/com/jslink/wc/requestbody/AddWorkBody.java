package com.jslink.wc.requestbody;

import lombok.Data;

import java.util.List;

@Data
public class AddWorkBody {
    private String poster;
    private Integer street;
    private String intro;
    private String phone;
    private String title;
    private String type; //音频类 视频类 图文类
//    private String cover;
//    private String[] attachments;
    private List<MediaBody> medias;
}
