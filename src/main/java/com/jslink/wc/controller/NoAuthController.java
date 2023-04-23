package com.jslink.wc.controller;

import com.jslink.wc.pojo.Dict;
import com.jslink.wc.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/wcapi/noauth")
public class NoAuthController extends BaseController {

    @Autowired
    private CommonService commonService;
    @Autowired
    private WorksService worksService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private OutstandingPeopleService peopleService;
    @Autowired
    private PopsciMgmtService popsciMgmtService;

    @PostMapping("/uploadfile")
    public String handleFileUpload(@RequestParam MultipartFile file) throws IOException {
        String fileName = commonService.saveFile(file);
        return fileName;
    }

    @PostMapping("/deletetempfile")
    public String deleteTempFile(@RequestParam String filePath) throws IOException {
        String fileName = commonService.deleteFile(filePath);
        return fileName;
    }

    @GetMapping("/dict")
    public List<Dict> getDict(){
        return commonService.getDict();
    }

    @GetMapping(value = "/print/{id}")
    public ResponseEntity<byte[]> printWorks(@PathVariable Integer id) throws IOException {
        return worksService.printWorks( id);
    }

    @GetMapping(value = "/print/{type}/{id}")
    public ResponseEntity<byte[]> printRecommendFile(
            @PathVariable String type,
            @PathVariable Integer id) throws IOException {
        if ("works".equals(type))
            return worksService.printWorks( id);
        else if ("brand".equals(type))
            return brandService.printBrand(id);
        else if ("mgmt".equals(type))
            return popsciMgmtService.printPopsci(id);
        else if ("people".equals(type))
            return peopleService.printPeople(id);
        else return null;
    }

    @GetMapping("/exportbrandexcel")
    public ResponseEntity<byte[]> exportBrandExcel() throws IOException {
        return brandService.exportExcel();
    }

    @GetMapping("/exportpeopleexcel")
    public ResponseEntity<byte[]> exportPeopleExcel() throws IOException {
        return peopleService.exportExcel();
    }

    @GetMapping("/exportmgmtexcel")
    public ResponseEntity<byte[]> exportMgmtExcel() throws IOException {
        return popsciMgmtService.exportExcel();
    }

    @GetMapping("/exportworksexcel")
    public ResponseEntity<byte[]> exportWorksExcel(
            @RequestParam(required = false) String poster,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer tjdw) throws IOException {
        return worksService.exportWorksExcel(poster, type, tjdw);
    }

    @PostMapping("/upload_reccform/{type}/{id}")
    public String uploadReccForm(@RequestParam MultipartFile file,@PathVariable String type, @PathVariable int id) throws IOException {
        if ("works".equals(type))
            return worksService.UploadReccForm(file, id);
        else if ("brand".equals(type))
            return brandService.UploadReccForm(file, id);
        else if ("mgmt".equals(type))
            return popsciMgmtService.UploadReccForm(file, id);
        else if ("people".equals(type))
            return peopleService.UploadReccForm(file, id);
        else return null;
    }
}
