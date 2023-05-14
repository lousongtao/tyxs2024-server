package com.jslink.wc.controller;

import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.responsebody.StatsBody;
import com.jslink.wc.service.BaseController;
import com.jslink.wc.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/wcapi/stats")
public class StatsController extends BaseController {
    @Autowired
    private StatsService statsService;

    @GetMapping()
    public PageResult<StatsBody> getStats(){
        Authentication authentication = authenticationFacade.getAuthentication();
        return statsService.getStats();
    }

}
