package com.jslink.wc.service;

import com.alibaba.fastjson.JSONObject;
import com.jslink.wc.pojo.Works;
import com.jslink.wc.pojo.WorksComment;
import com.jslink.wc.requestbody.AddWorkBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.responsebody.WorksBody;

import java.io.IOException;
import java.util.List;

public interface WorksService {
    PageResult<WorksBody> getWorks(String poster, Integer areaId, Integer streetId, int page, int pageSize);

    Works saveWorks(AddWorkBody body) throws IOException;

    List<WorksComment> updateWorks(String user, JSONObject json);
}
