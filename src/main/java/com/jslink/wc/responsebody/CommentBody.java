package com.jslink.wc.responsebody;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class CommentBody {
    private String expert;
    private JSONObject comment;
}
