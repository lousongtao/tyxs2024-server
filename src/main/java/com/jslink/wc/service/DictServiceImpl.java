package com.jslink.wc.service;

import com.jslink.wc.pojo.Dict;
import com.jslink.wc.repository.DictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DictServiceImpl implements DictService{
    @Autowired
    private DictRepository dictRepository;
    @Override
    public String getDict(String type, Integer value) {
        if (value == null) return null;
        Dict d = dictRepository.findByTypeAndValue(type, value);
        if (d != null) return d.getName();
        return null;
    }
}
