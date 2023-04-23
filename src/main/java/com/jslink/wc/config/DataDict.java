package com.jslink.wc.config;

import com.jslink.wc.pojo.Area;
import com.jslink.wc.pojo.Dict;
import com.jslink.wc.pojo.Street;
import com.jslink.wc.repository.AreaRepository;
import com.jslink.wc.repository.DictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataDict {
    @Autowired
    private AreaRepository areaRepository;
    @Autowired
    private DictRepository dictRepository;

//    private List<Dict> dicts;
    private List<Area> areas;
    private Map<Integer, Area> mapArea = new HashMap<>();
    private Map<Integer, Street> mapStreet = new HashMap<>();
    private Map<Integer, Integer> mapStreetArea = new HashMap<>(); //key = street.id   value=area.id

    @PostConstruct
    public void loadArea(){
//        areas = areaRepository.findAll();
//        areas.stream().forEach(area -> {
//            mapArea.put(area.getId(), area);
//            area.getStreets().stream().forEach(street -> {
//                mapStreet.put(street.getId(), street);
//                mapStreetArea.put(street.getId(), area.getId());
//            });
//        });
//        dicts = dictRepository.findAll();
    }

//    public List<Dict> getDicts() {
//        return dicts;
//    }

    public List<Area> getAreas(){
        return areas;
    }

    public Map<Integer, Area> getMapArea() {
        return mapArea;
    }

    public Map<Integer, Street> getMapStreet() {
        return mapStreet;
    }

    public Map<Integer, Integer> getMapStreetArea() {
        return mapStreetArea;
    }
}
