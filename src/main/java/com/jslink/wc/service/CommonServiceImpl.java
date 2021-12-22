package com.jslink.wc.service;

import com.jslink.wc.pojo.Area;
import com.jslink.wc.pojo.Dict;
import com.jslink.wc.repository.AreaRepository;
import com.jslink.wc.repository.DictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@Transactional
public class CommonServiceImpl implements CommonService{
    @Value("${fileupload_temp_savepath}")
    private String savepath;
    @Autowired
    private AreaRepository areaRepository;
    @Autowired
    private DictRepository dictRepository;
    @Override
    public List<Area> getAreas() {
        return areaRepository.findAll();
    }

    @Override
    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()){
            throw new IOException("Empty file exception");
        }
        Path target = Paths.get(savepath + "/" + System.currentTimeMillis() + "/" + file.getOriginalFilename());
        Files.createDirectories(target.getParent());
        Files.createFile(target);
        try(InputStream is = file.getInputStream()){
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target.toFile().getAbsolutePath();
    }

    @Override
    public String deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()){
            file.delete();
            String parent = file.getParent();
            File pFile = new File(parent);
            if (pFile.listFiles() == null || pFile.listFiles().length == 0)
                pFile.delete();
        }
        return "ok";
    }

    @Override
    public List<Dict> getDict() {
        return dictRepository.findAll();
    }

    @Override
    public void saveDict(String type, String value) {
        List<Dict> dicts = dictRepository.findAll();
        for(Dict dict : dicts){
            if (dict.getType().equals(type)){
                dict.setValue(Integer.parseInt(value));
                dictRepository.save(dict);
            }
        }
    }
}
