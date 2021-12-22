package com.jslink.wc.controller;

import com.jslink.wc.pojo.Area;
import com.jslink.wc.pojo.Dict;
import com.jslink.wc.service.BaseController;
import com.jslink.wc.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/wcapi/common")
public class CommonController extends BaseController {

    @Autowired
    private CommonService commonService;

    @GetMapping("/areas")
    public List<Area> getAreas(){
        return commonService.getAreas();
    }

    @PostMapping("/uploadfile")
    public String handleFileUpload(@RequestParam MultipartFile file) throws IOException {
        String fileName = commonService.saveFile(file);
        return fileName;
    }

    @PostMapping("/deletetempfile")
    public String deleteTempFile(@RequestParam String filePath) throws IOException {
        String fileName = commonService.deleteFile(filePath);
        return fileName;
    }

    @GetMapping("/dict")
    public List<Dict> getDict(){
        return commonService.getDict();
    }

    @PostMapping("/dict")
    public void saveDict(@RequestParam String type, @RequestParam String value){
        commonService.saveDict(type, value);
    }
}
