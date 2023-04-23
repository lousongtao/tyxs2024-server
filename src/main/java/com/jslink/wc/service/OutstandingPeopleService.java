package com.jslink.wc.service;

import com.alibaba.fastjson.JSONObject;
import com.jslink.wc.pojo.Brand;
import com.jslink.wc.pojo.OutstandingPeople;
import com.jslink.wc.pojo.Works;
import com.jslink.wc.requestbody.AddOutstandingPeopleBody;
import com.jslink.wc.requestbody.AddWorkBody;
import com.jslink.wc.responsebody.OutstandingPeopleBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.responsebody.WorksBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface OutstandingPeopleService {
    PageResult<OutstandingPeopleBody> getPeople(String accountName, String name, Integer applyType, String phone, String company, int page, int pageSize);

    OutstandingPeople savePeople(String accountName, AddOutstandingPeopleBody body) throws IOException;

    OutstandingPeople updatePeople(String accountName, Integer id, AddOutstandingPeopleBody body) throws IOException;

    OutstandingPeople returnPeople(String accountName, Integer id);

    ResponseEntity<byte[]> printPeople(Integer id) throws UnsupportedEncodingException;

    String UploadReccForm(MultipartFile file, int id) throws IOException;

    ResponseEntity<byte[]> exportExcel() throws IOException;
}
