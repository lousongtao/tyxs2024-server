package com.jslink.wc.controller;

import com.alibaba.fastjson.JSONObject;
import com.jslink.wc.pojo.Brand;
import com.jslink.wc.pojo.Works;
import com.jslink.wc.requestbody.AddWorkBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.responsebody.WorksBody;
import com.jslink.wc.service.BaseController;
import com.jslink.wc.service.WorksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/wcapi/works")
public class WorksController extends BaseController {
    @Autowired
    private WorksService worksService;

    @GetMapping()
    public PageResult<WorksBody> getWorks(@RequestParam(required = false) String poster,
                                              @RequestParam(required = false) Integer type,
                                              @RequestParam(required = false) Integer tjdw,
                                              @RequestParam int current,
                                              @RequestParam int pageSize){
        Authentication authentication = authenticationFacade.getAuthentication();
        return worksService.getWorks(authentication.getName(), poster, type, tjdw, current, pageSize);
    }

    @PostMapping()
    public Works addWorks(@RequestBody AddWorkBody body){
        Authentication authentication = authenticationFacade.getAuthentication();
        try {
            return worksService.saveWorks(authentication.getName(), body);
        } catch (IOException e) {
            return null;
        }
    }

    @PutMapping("/{id}")
    public Works updateWorks(@PathVariable Integer id, @RequestBody AddWorkBody body) throws IOException {
        Authentication authentication = authenticationFacade.getAuthentication();
        return worksService.updateWorks(authentication.getName(), id, body);
    }

    @PutMapping("/return/{id}")
    public Works returnWorks(@PathVariable Integer id) throws IOException {
        Authentication authentication = authenticationFacade.getAuthentication();
        return worksService.returnWorks(authentication.getName(), id);
    }
}
