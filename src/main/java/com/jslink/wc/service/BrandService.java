package com.jslink.wc.service;

import com.alibaba.fastjson.JSONObject;
import com.jslink.wc.pojo.Brand;
import com.jslink.wc.pojo.Works;
import com.jslink.wc.requestbody.AddBrandBody;
import com.jslink.wc.requestbody.AddWorkBody;
import com.jslink.wc.responsebody.BrandBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.responsebody.WorksBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface BrandService {
    PageResult<BrandBody> getBrand(String accountName, String name, int current, int pageSize);

    Brand save(String accountName, AddBrandBody body) throws IOException;

    Brand update(String accountName, Integer id, AddBrandBody body) throws IOException;

    Brand returnBrand(String accountName, Integer id);

    ResponseEntity<byte[]> printBrand(Integer id) throws UnsupportedEncodingException;

    String UploadReccForm(MultipartFile file, int id) throws IOException;

    ResponseEntity<byte[]> exportExcel() throws IOException;
}
