package com.jslink.wc.service;

import com.alibaba.fastjson.JSONObject;
import com.jslink.wc.pojo.Brand;
import com.jslink.wc.pojo.Works;
import com.jslink.wc.requestbody.AddWorkBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.responsebody.WorksBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface WorksService {
    PageResult<WorksBody> getWorks(String accountName, String poster, Integer type, Integer tjdw, int page, int pageSize);

    Works saveWorks(String accountName, AddWorkBody body) throws IOException;

    Works updateWorks(String user, Integer id, AddWorkBody body) throws IOException;

    ResponseEntity<byte[]> printWorks(Integer id) throws UnsupportedEncodingException;

    String UploadReccForm(MultipartFile file, int id) throws IOException;

    ResponseEntity<byte[]> exportWorksExcel(String poster, Integer type, Integer tjdw) throws IOException;

    Works returnWorks(String accountNameame, Integer id);
}
