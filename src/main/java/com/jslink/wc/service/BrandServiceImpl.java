package com.jslink.wc.service;

import com.jslink.wc.exception.DataCheckException;
import com.jslink.wc.pojo.*;
import com.jslink.wc.repository.*;
import com.jslink.wc.requestbody.AddBrandBody;
import com.jslink.wc.requestbody.PrizeBody;
import com.jslink.wc.requestbody.SubsidizeBody;
import com.jslink.wc.responsebody.BrandBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.util.Constants;
import com.jslink.wc.util.Utils;
import com.spire.doc.*;
import com.spire.doc.Table;
import com.spire.doc.collections.SectionCollection;
import com.spire.doc.documents.Paragraph;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BrandServiceImpl implements BrandService{
    @Value("${WorksFileDirectory}")
    private String fileDestDirectory;
    @Value("${template.recommend.brand}")
    private String templateRecommend;
    @Value("${WorksReccFormDirectory}")
    private String reccFormDirectory;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private BrandPrizeRepository prizeRepository;
    @Autowired
    private BrandSubsidizeRepository subsidizeRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private OrgTypeRepository orgTypeRepository;
    @Autowired
    private DictRepository dictRepository;
    @Override
    public PageResult<BrandBody> getBrand(String accountName, String name, int page, int pageSize) {
        //order by status desc
        Sort sort = Sort.by("status");
        Pageable pageable = PageRequest.of(page-1, pageSize, sort);
        List<Specification<Brand>> specs = new ArrayList<>();
        if (name != null && name.trim().length() > 0){
            specs.add((Specification<Brand>) (root, query, cb) -> cb.like(root.get("name"), "%" + name +"%"));
        }
        Account account = accountRepository.findByAccount(accountName);
        if (account.getType() == Constants.ACCOUNT_TYPE_UNIT1){
            List<Account> subs = accountRepository.findByParentAccountId(account.getId());
            List<Integer> subIds = subs.stream().map(sa -> sa.getId()).collect(Collectors.toList());
            specs.add((Specification<Brand>) (root, query, cb) -> root.get("account").get("id").in(subIds));
        } else if (account.getType() == Constants.ACCOUNT_TYPE_UNIT2){
            specs.add((Specification<Brand>) (root, query, cb) -> cb.equal(root.get("account").get("id"), account.getId()));
        }
        if (account.getType() == Constants.ACCOUNT_TYPE_ADMIN || account.getType() == Constants.ACCOUNT_TYPE_UNIT1){
            specs.add((Specification<Brand>) (root, query, cb) -> cb.equal(root.get("status"), Constants.WORKS_STATUS_SUBMIT));
        }

        Page<Brand> brands = null;
        if (specs.isEmpty()){
            brands = brandRepository.findAll(pageable);
        } else {
            Specification<Brand> spec = specs.get(0);
            for (int i = 1; i < specs.size(); i++) {
                spec = spec.and(specs.get(i));
            }
            brands = brandRepository.findAll(spec, pageable);
        }

        List<BrandBody> brandBodies = brands.getContent().stream().map(b -> {
            BrandBody body = new BrandBody();
            BeanUtils.copyProperties(b, body);
            body.setPrizeList(prizeRepository.findByBrandId(b.getId()));
            body.setSubsidizeList(subsidizeRepository.findByBrandId(body.getId()));
            return body;
        }).collect(Collectors.toList());
        return new PageResult<>(brandBodies, page, brands.getTotalElements());
    }

    /**
     * 新建, 对应的经历, 奖励, 资助等, 都用新建操作.
     * @param accountName
     * @param body
     * @return
     */
    @Override
    public Brand save(String accountName, AddBrandBody body) throws IOException {
        Account account = accountRepository.findByAccount(accountName);
        Brand b = new Brand();
        BeanUtils.copyProperties(body, b);
        //前端传递的文件路径, 统一转化为正斜线
        if (body.getFileUrl() != null) {
            String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
            b.setFileUrl(fileUrl);
        }
        b.setAccount(account);
        Brand dbb = brandRepository.save(b);
        //如果是提交状态, 搬迁文件, 成功后再保存记录一次
        if (body.getStatus() == Constants.WORKS_STATUS_SUBMIT){
            if (body.getFileUrl() != null && body.getFileUrl().length() > 0){
                String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
                String[] segs = fileUrl.split("/");
                String fileName = segs[segs.length - 1];
                String destDir = fileDestDirectory + getPersistFilePath(b);
                String destFile = Utils.moveFile(body.getFileUrl(), destDir);
                dbb.setFileUrl(destFile);
                dbb = brandRepository.save(dbb);
            }
        }
        createBrandPrize(dbb, body.getPrizeList());
        createBrandSubsidize(dbb, body.getSubsidizeList());
        return dbb;
    }

    private void createBrandSubsidize(Brand dbb, List<SubsidizeBody> subsidizeList) {
        if (subsidizeList != null){
            subsidizeList.stream().forEach(subsidize -> {
                BrandSubsidize bs = new BrandSubsidize();
                BeanUtils.copyProperties(subsidize, bs);
                bs.setBrand(dbb);
                subsidizeRepository.save(bs);
            });
        }
    }

    private void createBrandPrize(Brand dbb, List<PrizeBody> prizeList) {
        if (prizeList != null){
            prizeList.stream().forEach(prize -> {
                BrandPrize bp = new BrandPrize();
                BeanUtils.copyProperties(prize, bp);
                bp.setBrand(dbb);
                prizeRepository.save(bp);
            });
        }
    }


    /**
     * 修改, 对于下属的经历等子项, 先全部删除, 再全部新建.
     * @param accountName
     * @param id
     * @param body
     * @return
     */
    @Override
    public Brand update(String accountName, Integer id, AddBrandBody body) throws IOException {
        Brand b = brandRepository.findById(id).get();
        BeanUtils.copyProperties(body, b, "status");
        //前端传递的文件路径, 统一转化为正斜线
        b.setFileUrl(null);
        if (body.getFileUrl() != null) {
            String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
            b.setFileUrl(fileUrl);
        }
        if (b.getStatus() == Constants.WORKS_STATUS_DRAFT && body.getStatus() == Constants.WORKS_STATUS_SUBMIT){
            b.setStatus(Constants.WORKS_STATUS_SUBMIT);
            if (body.getFileUrl() != null && body.getFileUrl().length() > 0){
                String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
                String[] segs = fileUrl.split("/");
                String fileName = segs[segs.length - 1];
                String destDir = fileDestDirectory + getPersistFilePath(b);
                String destFile = Utils.moveFile(body.getFileUrl() , destDir);
                b.setFileUrl(destFile);
            }
        }
        b = brandRepository.save(b);
        prizeRepository.deleteByBrandId(id);
        subsidizeRepository.deleteByBrandId(id);
        createBrandPrize(b, body.getPrizeList());
        createBrandSubsidize(b, body.getSubsidizeList());
        return b;
    }

    @Override
    public Brand returnBrand(String accountName, Integer id) {
        Brand b = brandRepository.findById(id).get();
        if (b.getStatus() != Constants.WORKS_STATUS_SUBMIT){
            throw new DataCheckException(HttpStatus.FORBIDDEN, "该记录不在提交状态, 无法回退.");
        }
        b.setStatus(Constants.WORKS_STATUS_DRAFT);
        brandRepository.save(b);
        return b;
    }

    //因为type这个打印总是格式错乱, 这里将type分成7行, 每行只有一个选项, 避免行错乱.
    //该方法将判断type与输入值是否匹配, 匹配就显示选中状态
    private String getBrandTypePrintString(String type, String printText){
        if (type.equals(printText))
            return "■" + printText;
        else
            return "□" + printText;
    }

    @Override
    public ResponseEntity<byte[]> printBrand(Integer id) throws UnsupportedEncodingException {
        Brand brand = brandRepository.findById(id).get();
        Account postAccount = brand.getAccount();
        Account reccAccount = accountRepository.findById(postAccount.getParentAccountId()).get();
        Resource resource = new ClassPathResource(templateRecommend, this.getClass().getClassLoader());
        Document document = null;
        try {
            document = new Document(resource.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsupportedEncodingException("找不到打印模板, 请联系管理员.");
        }
        SectionCollection sc = document.getSections();
        Section section = sc.get(0);
        Map<String, String> map = new HashMap<>();
        map.put("name", brand.getName());
        String[] types = new String[]{
                "电视科普频道或栏目",
                "广播科普频段或栏目",
                "科普报刊或专栏",
                "科普网站",
                "科普新媒体",
                "健康科普公益品牌活动或讲座"
        };
        map.put("type1", getBrandTypePrintString(brand.getType(), "电视科普频道或栏目"));
        map.put("type2", getBrandTypePrintString(brand.getType(), "广播科普频段或栏目"));
        map.put("type3", getBrandTypePrintString(brand.getType(), "科普报刊或专栏"));
        map.put("type4", getBrandTypePrintString(brand.getType(), "科普网站"));
        map.put("type5", getBrandTypePrintString(brand.getType(), "科普新媒体"));
        map.put("type6", getBrandTypePrintString(brand.getType(), "健康科普公益品牌活动或讲座"));
        if (!"电视科普频道或栏目".equals(brand.getType())
                && !"广播科普频段或栏目".equals(brand.getType())
                && !"科普报刊或专栏".equals(brand.getType())
                && !"科普网站".equals(brand.getType())
                && !"科普新媒体".equals(brand.getType())
                && !"健康科普公益品牌活动或讲座".equals(brand.getType()))
            map.put("type7", getBrandTypePrintString(brand.getType(), brand.getType()));
        else
            map.put("type7", getBrandTypePrintString(brand.getType(), "其他"));
        if (brand.getCategory().equals("机构"))
            map.put("category", "■机构 □科室 □个人 □其他:");
        else if (brand.getCategory().equals("科室"))
            map.put("category", "□机构 ■科室 □个人 □其他:");
        else if (brand.getCategory().equals("个人"))
            map.put("category", "□机构 □科室 ■个人 □其他:");
        else
            map.put("category", "□机构 □科室 □个人 ■其他:" + brand.getCategory());
        map.put("company", brand.getCompany());
        map.put("contactPerson", brand.getContactPerson());
        map.put("phone", brand.getPhone());
        map.put("email", brand.getEmail());
        map.put("address", brand.getAddress());
        map.put("projectBrief", brand.getProjectBrief());
        map.put("projectDesc", brand.getProjectDesc());
        String orgType = null;
        if (reccAccount.getOrgTypeId() != null) {
            OrgType ot = orgTypeRepository.findById(reccAccount.getOrgTypeId()).get();
            orgType = ot.getName();
        }
        map.put("tjdw_tag", orgType);
        map.put("tjdw", reccAccount.getName());
        map.put("tjdw_contactor", reccAccount.getContactPerson());
        map.put("tjdw_phone", reccAccount.getPhone());
        map.put("tjdw_email", reccAccount.getEmail());

        for(Object obj : section.getTables()){
            Table table = (Table)obj;
            table.autoFit(AutoFitBehaviorType.Fixed_Column_Widths);
            for(TableRow row:(Iterable<TableRow>)table.getRows()){
                for(TableCell cell : (Iterable<TableCell>)row.getCells()){
                    for(Paragraph para : (Iterable<Paragraph>)cell.getParagraphs()){
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            para.replace("${" + entry.getKey() + "}", entry.getValue()==null? "": entry.getValue(), false, true);
                        }
                    }
                }
            }
        }
        List<BrandPrize> prizeList = prizeRepository.findByBrandId(id);
        Table table = Utils.findTableByKeyword(document, "曾获科普奖励情况");
        Utils.addPrizeRow(table, prizeList);


        List<BrandSubsidize> subsidizeList = subsidizeRepository.findByBrandId(id);
        Table tableSub = Utils.findTableByKeyword(document, "曾获计划资助情况");
        Utils.addSubsidizeRow(tableSub, subsidizeList);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        document.saveToStream(os, FileFormat.Doc);
        String fileName = postAccount.getName() + "-" + brand.getId() + ".doc";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename="+ URLEncoder.encode(fileName, "UTF-8"));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(os.size())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(os.toByteArray());
    }

    @Override
    public String UploadReccForm(MultipartFile file, int id) throws IOException {
        if (file.isEmpty()){
            throw new IOException("Empty file exception");
        }
        //如果之前曾上传过推荐表, 先把原文件删除.
        Brand brand = brandRepository.findById(id).get();
        if (brand.getReccFormFileUrl() != null){
            File old = new File(brand.getReccFormFileUrl());
            old.deleteOnExit();
        }
        //中文文件名在Linux上报错, 这里统一改成reccform
        String[] segs = file.getOriginalFilename().split("\\.");
        String newName = "reccform."+segs[segs.length - 1];
        Path target = Paths.get(reccFormDirectory + getPersistFilePath(brand) + "/" + id + "/" + newName);
        //如果该文件已存在, 就修改文件名, 否则会出现重名文件冲突
        int i = 0;
        while(target.toFile() != null && target.toFile().exists()){
            newName = "reccform(" + (++i) + ")."+segs[segs.length - 1];
            target = Paths.get(reccFormDirectory + getPersistFilePath(brand) + "/" + id + "/" + newName);
        }
        Files.createDirectories(target.getParent());
        Files.createFile(target);
        try(InputStream is = file.getInputStream()){
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }
        String path = target.toFile().getAbsolutePath();
        //前端传递的文件路径, 统一转化为正斜线
        path = path.replaceAll("\\\\", "/");
        brand.setReccFormFileUrl(path);
        brandRepository.save(brand);
        return path;
    }

    private String getPersistFilePath(Brand brand){
        Account sbdw = brand.getAccount();
        Account tjdw = accountRepository.findById(sbdw.getParentAccountId()).get();
        OrgType ot = orgTypeRepository.findById(tjdw.getOrgTypeId()).get();
        return "/".concat(ot.getName()).concat("/").concat(tjdw.getName()).concat("/").concat(sbdw.getName())
                .concat("/科普品牌/").concat(brand.getId().toString());
    }

    //首行合并, 并设置特有单元格大小
    private Row getTitleRow(Sheet sheet, int width){
        CellStyle cs = sheet.getWorkbook().createCellStyle();
        cs.setAlignment(HorizontalAlignment.CENTER);
        Font font = sheet.getWorkbook().createFont();
        font.setFontHeight((short)360);
        font.setBold(true);
        font.setFontName("宋体");
        cs.setFont(font);

        Row row = sheet.createRow(0);
        row.setHeight((short)500);
        for (int i = 0; i < width; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(cs);
        }
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, width - 1));
        Cell cell = row.getCell(0);
        cell.setCellValue("2022年“上海市健康科普推优选树”科普品牌推荐汇总表");

        return row;
    }

    //
    private Row getHeaderRow(Sheet sheet, String[] headers){
        CellStyle cs = sheet.getWorkbook().createCellStyle();
        cs.setAlignment(HorizontalAlignment.CENTER);
        Font font = sheet.getWorkbook().createFont();
        font.setFontHeight((short)240);
        font.setFontName("宋体");
        font.setBold(true);
        cs.setFont(font);
        Row row = sheet.createRow(1);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(cs);
            cell.setCellValue(headers[i]);
        }
        return row;
    }

    private Object[] pickupExportValue(Brand brand, List<Account> accounts, List<OrgType> orgTypes, int length){
        Object[] objs = new Object[length];
        Account postAcc = brand.getAccount();
        if (postAcc != null){
            objs[2] = postAcc.getName();
            for(Account tj: accounts){
                if (tj.getId().equals(postAcc.getParentAccountId())){
                    objs[1] = tj.getName();
                    for(OrgType ot : orgTypes){
                        if (ot.getId().equals(tj.getOrgTypeId()))
                            objs[0] = ot.getName();
                    }
                    break;
                }
            }
        }
        if (brand.getFileUrl() != null && brand.getFileUrl().indexOf(Constants.DIRECTORY_WORKS_FILES) > 0)
            objs[3] = Constants.PUBLISH_DOMAIN + "/" + brand.getFileUrl().substring(brand.getFileUrl().indexOf(Constants.DIRECTORY_WORKS_FILES));
        objs[4] = brand.getName();
        objs[5] = brand.getType();
        objs[6] = brand.getCategory();
        objs[7] = brand.getCompany();
        objs[8] = brand.getContactPerson();
        objs[9] = brand.getPhone();
        objs[10] = brand.getAddress();
        objs[11] = brand.getEmail();

        return objs;
    }

    @Override
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        //                                0        1          2         3          4         5       6        7         8        9     10           11
        String[] headers = new String[]{"机构类型", "推荐单位", "申报单位", "本地链接", "品牌名称", "类型", "归属类别","所在单位","联系人","手机", "通讯地址", "电子邮箱"};
        int [] columnWidth = new int[]{  7000,     7000,      7000,     13000,     8000,     8000,   4000,    8000,     4000,   6000,   12000,   7000};
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("科普品牌");
        Row rowTitle = getTitleRow(sheet, headers.length);
        Row rowHeader = getHeaderRow(sheet, headers);
        CellStyle cs = wb.createCellStyle();
        cs.setAlignment(HorizontalAlignment.LEFT);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        cs.setWrapText(true);
        Font font = sheet.getWorkbook().createFont();
        font.setFontHeight((short)240);
        font.setFontName("宋体");
        cs.setFont(font);
        List<Brand> brands = brandRepository.findByStatus(Constants.WORKS_STATUS_SUBMIT);
        List<Account> accounts = accountRepository.findAll();
        List<OrgType> orgTypes = orgTypeRepository.findAll();
        List<Dict> dicts = dictRepository.findAll();
        for (int i = 0; i < brands.size(); i++) {
            Row row = sheet.createRow(i+2);
            Object[] objs = pickupExportValue(brands.get(i), accounts, orgTypes, headers.length);
            for (int j = 0; j < headers.length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellStyle(cs);
                String objectValue = objs[j] == null ? "" : objs[j].toString();
                cell.setCellValue(objectValue);
                if (objectValue.startsWith("http")){
                    Hyperlink link = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
                    link.setAddress(objectValue);
                    link.setFirstRow(row.getRowNum());
                    link.setLastRow(row.getRowNum());
                    link.setFirstColumn(cell.getColumnIndex());
                    link.setLastColumn(cell.getColumnIndex());
                    cell.setHyperlink(link);
                }
            }
        }
        for (int i = 0; i < columnWidth.length; i++) {
            sheet.setColumnWidth(i, columnWidth[i]);
        }
        String fileName = "科普品牌导出-" + Constants.DFYMD.format(new Date())+ ".xls";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
        httpHeaders.add("Content-Disposition", "attachment; filename="+ URLEncoder.encode(fileName, "UTF-8"));
        httpHeaders.add("Pragma", "no-cache");
        httpHeaders.add("Expires", "0");
        httpHeaders.add("Last-Modified", new Date().toString());
        httpHeaders.add("ETag", String.valueOf(System.currentTimeMillis()));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .contentLength(os.size())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(os.toByteArray());
    }

}
