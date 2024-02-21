package com.jslink.wc.controller;

import com.jslink.wc.pojo.Dict;
import com.jslink.wc.pojo.OrgType;
import com.jslink.wc.service.BaseController;
import com.jslink.wc.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/wcapi/common")
public class CommonController extends BaseController {

    @Autowired
    private CommonService commonService;

    @GetMapping("/dict")
    public List<Dict> getDict(){
        Authentication authentication = authenticationFacade.getAuthentication();
        return commonService.getDict();
    }

    @PostMapping("/dict")
    public void saveDict(@RequestParam String type, @RequestParam String value){
        Authentication authentication = authenticationFacade.getAuthentication();
        commonService.saveDict(authentication.getName(), type, value);
    }

    @GetMapping("/orgtype")
    public List<OrgType> getOrgType(){
        return commonService.getOrgType();
    }
}
