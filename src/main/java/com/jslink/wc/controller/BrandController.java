package com.jslink.wc.controller;

import com.alibaba.fastjson.JSONObject;
import com.jslink.wc.pojo.Brand;
import com.jslink.wc.pojo.Works;
import com.jslink.wc.requestbody.AddBrandBody;
import com.jslink.wc.requestbody.AddWorkBody;
import com.jslink.wc.responsebody.BrandBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.responsebody.WorksBody;
import com.jslink.wc.service.BaseController;
import com.jslink.wc.service.BrandService;
import com.jslink.wc.service.WorksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/wcapi/brand")
public class BrandController extends BaseController {
    @Autowired
    private BrandService brandService;

    @GetMapping()
    public PageResult<BrandBody> getBrand(@RequestParam(required = false) String name,
                                          @RequestParam(required = false) String accountName,
                                          @RequestParam int current,
                                          @RequestParam int pageSize){
        Authentication authentication = authenticationFacade.getAuthentication();
        return brandService.getBrand(authentication.getName(), name, accountName, current, pageSize);
    }

    @PostMapping()
    public Brand addBrand(@RequestBody AddBrandBody body) throws IOException {
        Authentication authentication = authenticationFacade.getAuthentication();
        return brandService.save(authentication.getName(), body);
    }

    @PutMapping("/{id}")
    public Brand updateBrand(@PathVariable Integer id, @RequestBody AddBrandBody body) throws IOException {
        Authentication authentication = authenticationFacade.getAuthentication();
        return brandService.update(authentication.getName(), id, body);
    }

    @PutMapping("/return/{id}")
    public Brand returnBrand(@PathVariable Integer id, @RequestParam String returnReason) throws IOException {
        Authentication authentication = authenticationFacade.getAuthentication();
        return brandService.returnBrand(authentication.getName(), id, returnReason);
    }
}
