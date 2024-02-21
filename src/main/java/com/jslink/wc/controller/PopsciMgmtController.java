package com.jslink.wc.controller;

import com.alibaba.fastjson.JSONObject;
import com.jslink.wc.pojo.Brand;
import com.jslink.wc.pojo.PopsciMgmt;
import com.jslink.wc.requestbody.AddBrandBody;
import com.jslink.wc.requestbody.AddPopsciMgmtBody;
import com.jslink.wc.responsebody.BrandBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.responsebody.PopsciMgmtBody;
import com.jslink.wc.service.BaseController;
import com.jslink.wc.service.BrandService;
import com.jslink.wc.service.PopsciMgmtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/wcapi/popsci")
public class PopsciMgmtController extends BaseController {
    @Autowired
    private PopsciMgmtService popsciMgmtService;

    @GetMapping()
    public PageResult<PopsciMgmtBody> getPopsci(@RequestParam(required = false) Integer applyType,
                                                @RequestParam(required = false) String deptName,
                                                @RequestParam(required = false) String name,
                                               @RequestParam int current,
                                               @RequestParam int pageSize){
        Authentication authentication = authenticationFacade.getAuthentication();
        return popsciMgmtService.getPopsci(authentication.getName(), applyType, deptName, name, current, pageSize);
    }

    @PostMapping()
    public PopsciMgmt addPopsci(@RequestBody AddPopsciMgmtBody body) throws IOException {
        Authentication authentication = authenticationFacade.getAuthentication();
        return popsciMgmtService.save(authentication.getName(), body);
    }

    @PutMapping("/{id}")
    public PopsciMgmt updatePopsci(@PathVariable Integer id, @RequestBody AddPopsciMgmtBody body) throws IOException {
        Authentication authentication = authenticationFacade.getAuthentication();
        return popsciMgmtService.update(authentication.getName(), id, body);
    }

    @PutMapping("/return/{id}")
    public PopsciMgmt returnPopsci(@PathVariable Integer id, @RequestParam String returnReason) throws IOException {
        Authentication authentication = authenticationFacade.getAuthentication();
        return popsciMgmtService.returnPopsci(authentication.getName(), id, returnReason);
    }
}
