package com.jslink.wc.controller;

import com.alibaba.fastjson.JSONObject;
import com.jslink.wc.pojo.Works;
import com.jslink.wc.pojo.WorksComment;
import com.jslink.wc.requestbody.AddWorkBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.responsebody.WorksBody;
import com.jslink.wc.service.BaseController;
import com.jslink.wc.service.WorksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/wcapi/works")
public class WorksController extends BaseController {
    @Autowired
    private WorksService worksService;

    @GetMapping()
    public PageResult<WorksBody> getWorks(@RequestParam(required = false) String poster,
                                              @RequestParam(required = false) Integer area,
                                              @RequestParam(required = false) Integer street,
                                              @RequestParam int current,
                                              @RequestParam int pageSize){
        return worksService.getWorks(poster, area, street, current, pageSize);
    }

    @PostMapping()
    public Works addWorks(@RequestBody AddWorkBody body){
        try {
            return worksService.saveWorks(body);
        } catch (IOException e) {
            return null;
        }
    }

    @PutMapping()
    public List<WorksComment> updateWorks(@RequestBody JSONObject json, Authentication authentication){
//        Authentication authentication = authenticationFacade.getAuthentication();
        return worksService.updateWorks("", json);
    }
}
