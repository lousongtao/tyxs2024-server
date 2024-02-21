package com.jslink.wc.service;

import com.alibaba.fastjson.JSONObject;
import com.jslink.wc.pojo.PopsciMgmt;
import com.jslink.wc.pojo.Works;
import com.jslink.wc.requestbody.AddBrandBody;
import com.jslink.wc.requestbody.AddPopsciMgmtBody;
import com.jslink.wc.requestbody.AddWorkBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.responsebody.PopsciMgmtBody;
import com.jslink.wc.responsebody.WorksBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface PopsciMgmtService {
    PopsciMgmt update(String accountName, Integer id, AddPopsciMgmtBody body) throws IOException;

    PopsciMgmt save(String accountName, AddPopsciMgmtBody body) throws IOException;

    PageResult<PopsciMgmtBody> getPopsci(String accountName, Integer applyType, String deptName, String name, int currentPage, int pageSize);

    PopsciMgmt returnPopsci(String accountName, Integer id, String returnReason);

    ResponseEntity<byte[]> printPopsci(Integer id) throws UnsupportedEncodingException;

    String UploadReccForm(MultipartFile file, int id) throws IOException;

    ResponseEntity<byte[]> exportExcel() throws IOException;
}
