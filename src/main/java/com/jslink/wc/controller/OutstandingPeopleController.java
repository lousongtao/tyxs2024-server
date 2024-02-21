package com.jslink.wc.controller;

import com.alibaba.fastjson.JSONObject;
import com.jslink.wc.pojo.Brand;
import com.jslink.wc.pojo.OutstandingPeople;
import com.jslink.wc.requestbody.AddBrandBody;
import com.jslink.wc.requestbody.AddOutstandingPeopleBody;
import com.jslink.wc.responsebody.BrandBody;
import com.jslink.wc.responsebody.OutstandingPeopleBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.service.BaseController;
import com.jslink.wc.service.BrandService;
import com.jslink.wc.service.OutstandingPeopleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/wcapi/people")
public class OutstandingPeopleController extends BaseController {
    @Autowired
    private OutstandingPeopleService outstandingPeopleService;

    @GetMapping()
    public PageResult<OutstandingPeopleBody> getPeople(@RequestParam(required = false) String name,
                                                       @RequestParam(required = false) Integer applyType,
                                                       @RequestParam(required = false) String phone,
                                                       @RequestParam(required = false) String company,
                                                       @RequestParam int current,
                                                       @RequestParam int pageSize){
        Authentication authentication = authenticationFacade.getAuthentication();
        return outstandingPeopleService.getPeople(authentication.getName(), name, applyType, phone, company, current, pageSize);
    }

    @PostMapping()
    public OutstandingPeople addPeople(@RequestBody AddOutstandingPeopleBody body) throws IOException {
        Authentication authentication = authenticationFacade.getAuthentication();
        return outstandingPeopleService.savePeople(authentication.getName(), body);
    }

    @PutMapping("/{id}")
    public OutstandingPeople updatePeople(@PathVariable Integer id, @RequestBody AddOutstandingPeopleBody body) throws IOException {
        Authentication authentication = authenticationFacade.getAuthentication();
        return outstandingPeopleService.updatePeople(authentication.getName(), id, body);
    }

    @PutMapping("/return/{id}")
    public OutstandingPeople returnPeople(@PathVariable Integer id, @RequestParam String returnReason) throws IOException {
        Authentication authentication = authenticationFacade.getAuthentication();
        return outstandingPeopleService.returnPeople(authentication.getName(), id, returnReason);
    }
}
