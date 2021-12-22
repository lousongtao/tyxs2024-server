package com.jslink.wc.responsebody;

import com.jslink.wc.requestbody.MediaBody;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WorksBody {
    private int id;
    private Date createDate;
    private String poster;
    private String title;
//    private String street;
//    private String area;
    private String intro;
    private String phone;
    private String type;
    private List<MediaBody> medias;
//    private int status;
//    private String coverPath;
//    private List<String> attachmentsPath = new ArrayList<>();
//    private List<CommentBody> comments = new ArrayList<>();
}
