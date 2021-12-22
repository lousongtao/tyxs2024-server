package com.jslink.wc.service;

import com.jslink.wc.pojo.Area;
import com.jslink.wc.pojo.Dict;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CommonService {
    List<Area> getAreas();

    String saveFile(MultipartFile file) throws IOException;

    String deleteFile(String filePath);

    List<Dict> getDict();

    void saveDict(String type, String value);
}
