package com.jslink.wc.service;

import com.jslink.wc.exception.ForbiddenException;
import com.jslink.wc.pojo.*;
import com.jslink.wc.repository.*;
import com.jslink.wc.util.Constants;
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
    private DictRepository dictRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private WorksRepository worksRepository;
    @Autowired
    private OrgTypeRepository orgTypeRepository;

    @Override
    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()){
            throw new IOException("Empty file exception");
        }
//        System.setProperty("sun.jnu.encoding", "utf-8");//保证中文名路径可以操作
        //文件名包含中文字符时, 在Linux上会报错
//        String[] segs = file.getOriginalFilename().split("\\.");
//        String extend = "." + segs[segs.length - 1];

        Path target = Paths.get(savepath + "/" + System.currentTimeMillis() + "/" + file.getOriginalFilename());
        Files.createDirectories(target.getParent());
        Files.createFile(target);
        try(InputStream is = file.getInputStream()){
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target.toFile().getAbsolutePath();
    }

    /**
     * 删除文件时, 如果该文件已经被某个作品使用, 要清除这个作品的字段
     * 文件删除后, 如果该目录已空, 把目录也删除掉
     * @param filePath
     * @return
     */
    @Override
    public String deleteFile(String filePath) {
        List<Works> works = worksRepository.findByFileUrl(filePath);
        works.forEach(w -> {
            w.setFileUrl(null);
            worksRepository.save(w);
        });
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
    public void saveDict(String user, String type, String value) {
        Account account = accountRepository.findByAccount(user);
        if (account.getType() != Constants.ACCOUNT_TYPE_ADMIN)
            throw new ForbiddenException("没有权限");
        List<Dict> dicts = dictRepository.findAll();
        for(Dict dict : dicts){
            if (dict.getType().equals(type)){
                dict.setValue(Integer.parseInt(value));
                dictRepository.save(dict);
            }
        }
    }

    @Override
    public List<OrgType> getOrgType() {
        return orgTypeRepository.findAll();
    }
}
