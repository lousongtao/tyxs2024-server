package com.jslink.wc.service;

import com.alibaba.fastjson.JSONObject;
import com.jslink.wc.config.DataDict;
import com.jslink.wc.exception.DataCheckException;
import com.jslink.wc.exception.ForbiddenException;
import com.jslink.wc.exception.NotFoundException;
import com.jslink.wc.pojo.*;
import com.jslink.wc.repository.*;
import com.jslink.wc.requestbody.AddWorkBody;
import com.jslink.wc.requestbody.PrizeBody;
import com.jslink.wc.requestbody.SubsidizeBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.responsebody.WorksBody;
import com.jslink.wc.util.Constants;
import com.jslink.wc.util.Utils;
import com.spire.doc.*;
import com.spire.doc.Table;
import com.spire.doc.collections.SectionCollection;
import com.spire.doc.documents.Paragraph;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFCreationHelper;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
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
public class WorksServiceImpl implements WorksService{
    @Value("${WorksFileDirectory}")
    private String fileDestDirectory;
    @Value("${template.recommend.works}")
    private String templateRecommend;
    @Value("${WorksReccFormDirectory}")
    private String reccFormDirectory;
    @Autowired
    private OrgTypeRepository orgTypeRepository;
    @Autowired
    private DictRepository dictRepository;
    @Autowired
    private WorksRepository worksRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private DataDict dataDict;
    @Autowired
    private WorksSubsidizeRepository subsidizeRepository;
    @Autowired
    private WorksPrizeRepository prizeRepository;

    private List<Account> accounts = new ArrayList<>();
    private List<OrgType> orgTypes = new ArrayList<>();

    /**
     * 草稿阶段的作品, 只有上传帐号自己能看到
     * @param accountName 当前操作人, 如果是管理员, 就查全部数据, 如果是推荐帐号, 查询对应的所有提交帐号上传的作品, 如果是提交帐号, 只能查询自己提交的
     * @param poster
     * @param page 客户端传入的page值从1开始, 服务端使用0开始, 所以使用时先把page减1
     * @param pageSize
     * @return
     */
    @Override
    public PageResult<WorksBody> getWorks(String accountName, String poster, Integer type, Integer tjdw, int page, int pageSize) {
        //order by status desc
        Sort sort = Sort.by("status");
        Pageable pageable = PageRequest.of(page-1, pageSize, sort);
        List<Specification<Works>> specs = new ArrayList<>();
        if (poster != null && poster.trim().length() > 0){
            specs.add((Specification<Works>) (root, query, cb) -> cb.like(root.get("poster"), "%" + poster + "%"));
        }
        if (type != null){
            specs.add((Specification<Works>) (root, query, cb) -> cb.equal(root.get("type"), type));
        }
        if (tjdw != null){
            List<Account> accSbdws = accountRepository.findByParentAccountId(tjdw);
            List<Integer> accids = accSbdws.stream().map(a -> a.getId()).collect(Collectors.toList());
            specs.add((Specification<Works>) (root, query, cb) -> root.get("account").get("id").in(accids));
        }

        Account account = accountRepository.findByAccount(accountName);
        if (account.getType() == Constants.ACCOUNT_TYPE_UNIT1){
            List<Account> subs = accountRepository.findByParentAccountId(account.getId());
            List<Integer> subIds = subs.stream().map(sa -> sa.getId()).collect(Collectors.toList());
            specs.add((Specification<Works>) (root, query, cb) -> root.get("account").get("id").in(subIds));
        } else if (account.getType() == Constants.ACCOUNT_TYPE_UNIT2){
            specs.add((Specification<Works>) (root, query, cb) -> cb.equal(root.get("account").get("id"), account.getId()));
        }
        if (account.getType() == Constants.ACCOUNT_TYPE_ADMIN || account.getType() == Constants.ACCOUNT_TYPE_UNIT1){
            specs.add((Specification<Works>) (root, query, cb) -> cb.equal(root.get("status"), Constants.WORKS_STATUS_SUBMIT));
        }
        Page<Works> works = null;
        if (specs.isEmpty())
            works = worksRepository.findAll(pageable);
        else {
            Specification<Works> spec = specs.get(0);
            for (int i = 1; i < specs.size(); i++) {
                spec = spec.and(specs.get(i));
            }
            works = worksRepository.findAll(spec, pageable);
        }

        List<WorksBody> worksBodies = works.getContent().stream().map(w -> {
            WorksBody wb = new WorksBody();
            BeanUtils.copyProperties(w, wb);
            wb.setPrizeList(prizeRepository.findByWorksId(w.getId()));
            wb.setSubsidizeList(subsidizeRepository.findByWorksId(w.getId()));
            return wb;
        }).collect(Collectors.toList());

        return new PageResult(worksBodies, page, works.getTotalElements());
    }

    /**
     * 新建works的时候, 把用户放在缓存文件夹中的文件拷贝出来, 以新建的works.id为目录. 拷贝后将缓存的目录清空
     * 如果works的状态是'submit', 就把临时文件中的文档复制到正式目录中,
     * @param body
     * @return
     */
    @Override
    public Works saveWorks(String accountName, AddWorkBody body) throws IOException {
        Account account = accountRepository.findByAccount(accountName);
        Works works = new Works();
        BeanUtils.copyProperties(body, works);
        works.setCreateDate(new Date());
        //前端传递的文件路径, 统一转化为正斜线
        if (body.getFileUrl() != null) {
            String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
            works.setFileUrl(fileUrl);
        }
        works.setAccount(account);
        Works newWorks = worksRepository.save(works);
        //如果是提交状态, 搬迁文件, 成功后再保存记录一次
        if (body.getStatus() == Constants.WORKS_STATUS_SUBMIT){
            if (body.getFileUrl() != null && body.getFileUrl().length() > 0){
                String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
                String[] segs = fileUrl.split("/");
                String fileName = segs[segs.length - 1];
                String destDir = fileDestDirectory + getPersistFilePath(newWorks);
                String destFile = Utils.moveFile(body.getFileUrl(), destDir);
                newWorks.setFileUrl(destFile);
                newWorks = worksRepository.save(newWorks);
            }
        }
        createPrize(works, body.getPrizeList());
        createSubsidize(works, body.getSubsidizeList());
        return newWorks;
    }

    private void createSubsidize(Works works, List<SubsidizeBody> subsidizeList) {
        if (subsidizeList != null){
            subsidizeList.stream().forEach(subsidize -> {
                WorksSubsidize ws = new WorksSubsidize();
                BeanUtils.copyProperties(subsidize, ws);
                ws.setWorks(works);
                subsidizeRepository.save(ws);
            });
        }
    }

    private void createPrize(Works works, List<PrizeBody> prizeList) {
        if (prizeList != null){
            prizeList.stream().forEach(prize -> {
                WorksPrize wp = new WorksPrize();
                BeanUtils.copyProperties(prize, wp);
                wp.setWorks(works);
                prizeRepository.save(wp);
            });
        }
    }

    private String getPersistFilePath(Works works){
        Account sbdw = works.getAccount();
        Account tjdw = accountRepository.findById(sbdw.getParentAccountId()).get();
        OrgType ot = orgTypeRepository.findById(tjdw.getOrgTypeId()).get();
        return "/".concat(ot.getName()).concat("/").concat(tjdw.getName()).concat("/").concat(sbdw.getName())
                        .concat("/科普作品/").concat(works.getId().toString());
    }

    /**
     * update 作品, 如果状态改为'submit', 就把放在临时文件目录中的文件, 搬迁到正式的目录中
     * @param user
     * @return
     */
    @Override
    public Works updateWorks(String user, Integer id, AddWorkBody body) throws IOException {
        final Works works = worksRepository.findById(id).get();
        //首先使用工具拷贝属性, 特殊属性分别在copyProperties后面处理
        BeanUtils.copyProperties(body, works, "status");
        //前端传递的文件路径, 统一转化为正斜线
        works.setFileUrl(null);
        if (body.getFileUrl() != null) {
            String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
            works.setFileUrl(fileUrl);
        }
        if (works.getStatus() == Constants.WORKS_STATUS_DRAFT && body.getStatus() == Constants.WORKS_STATUS_SUBMIT){
            works.setStatus(Constants.WORKS_STATUS_SUBMIT);
            if (body.getFileUrl() != null && body.getFileUrl().length() > 0){
                String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
                String[] segs = fileUrl.split("/");
                String fileName = segs[segs.length - 1];
                String destDir = fileDestDirectory + getPersistFilePath(works);
                String destFile = Utils.moveFile(body.getFileUrl() , destDir);
                works.setFileUrl(destFile);
            }
        }
        works.setUpdateDate(new Date());
        worksRepository.save(works);

        prizeRepository.deleteByWorksId(id);
        subsidizeRepository.deleteByWorksId(id);
        createPrize(works, body.getPrizeList());
        createSubsidize(works, body.getSubsidizeList());

        return works;
    }

    //根据模板打印作品成Word文档
    @Override
    public ResponseEntity<byte[]> printWorks(Integer id) throws UnsupportedEncodingException {
        Optional<Works> optWorks = worksRepository.findById(id);
        if (!optWorks.isPresent()) throw new NotFoundException("未找到数据, id="+id);
        Works works = optWorks.get();
        Account postAccount = works.getAccount();
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
        map.put("title", formatWorksValue(works.getTitle(), 1500));
        map.put("vendor", works.getVendor());
        getWorksType(works, map);
        map.put("phone", works.getPhone());
        map.put("poster", works.getPoster());
        String orgType = null;
        if (reccAccount.getOrgTypeId() != null) {
            OrgType ot = orgTypeRepository.findById(reccAccount.getOrgTypeId()).get();
            orgType = ot.getName();
        }
        map.put("tjdw_tag", formatWorksValue(orgType, 2000));
        map.put("tjdw", formatWorksValue(reccAccount.getName(), 1800));
        map.put("tjdw_contactor", reccAccount.getContactPerson());
        map.put("tjdw_phone", reccAccount.getPhone());
        map.put("tjdw_email", reccAccount.getEmail());
        map.put("zzdw", formatWorksValue(postAccount.getName(), 5000));
        map.put("mediaName", formatWorksValue(works.getMediaName(), 2000));
        map.put("subMediaName", formatWorksValue(works.getSubMediaName(), 2000));
        map.put("playDate", formatWorksValue(works.getMediaPlayDate(), 0));
        map.put("playTimes", formatWorksValue(works.getMediaPlayTimes(), 0));
        map.put("link", formatWorksValue(works.getMediaLink(), 25000));
        map.put("describe", formatWorksValue(works.getIntro(), 10000));
        map.put("projectBrief", works.getProjectBrief());
        map.put("projectDesc", works.getProjectDesc());


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

        List<WorksPrize> prizeList = prizeRepository.findByWorksId(id);
        final Table table1 = Utils.findTableByKeyword(document, "曾获科普奖励情况");
        Utils.addPrizeRow(table1, prizeList);

        List<WorksSubsidize> subsidizeList = subsidizeRepository.findByWorksId(id);
        final Table tableSub = Utils.findTableByKeyword(document, "曾获计划资助情况");
        Utils.addSubsidizeRow(tableSub, subsidizeList);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        document.saveToStream(os, FileFormat.Doc);
        String fileName = postAccount.getName() + "-" + works.getPoster() + "-" + works.getId() + ".doc";
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
        Works works = worksRepository.findById(id).get();
        if (works.getReccFormFileUrl() != null){
            File old = new File(works.getReccFormFileUrl());
            old.deleteOnExit();
        }
        //中文文件名在Linux上报错, 这里统一改成reccform
        String[] segs = file.getOriginalFilename().split("\\.");
        String newName = "reccform."+segs[segs.length - 1];
        Path target = Paths.get(reccFormDirectory + getPersistFilePath(works) + "/" + id + "/" + newName);
        //如果该文件已存在, 就修改文件名, 否则会出现重名文件冲突
        int i = 0;
        while(target.toFile() != null && target.toFile().exists()){
            newName = "reccform(" + (++i) + ")."+segs[segs.length - 1];
            target = Paths.get(reccFormDirectory + getPersistFilePath(works) + "/" + id + "/" + newName);
        }
        Files.createDirectories(target.getParent());
        Files.createFile(target);
        try(InputStream is = file.getInputStream()){
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }
        String path = target.toFile().getAbsolutePath();
        //前端传递的文件路径, 统一转化为正斜线
        path = path.replaceAll("\\\\", "/");
        works.setReccFormFileUrl(path);
        worksRepository.save(works);
        return path;
    }

    private Object[] pickupWorksValue(Works works, List<Account> accounts, List<OrgType> orgTypes, List<Dict> dicts, int length){
        Object[] objs = new Object[length];
        Account postAcc = works.getAccount();
        if (postAcc != null){
            objs[5] = postAcc.getName();
            for(Account tj: accounts){
                if (tj.getId().equals(postAcc.getParentAccountId())){
                    objs[6] = tj.getName();
                    objs[14] = tj.getPhone();
                    for(OrgType ot : orgTypes){
                        if (ot.getId().equals(tj.getOrgTypeId()))
                            objs[0] = ot.getName();
                    }
                    break;
                }
            }
        }
        for(Dict d: dicts){
            if (d.getType().equals(Constants.WORK_TYPE) && d.getValue().equals(works.getType())) {
                objs[2] = d.getName();
                if (d.getValue().intValue() < 20)
                    objs[1] = "图文类";
                else if (d.getValue().intValue() < 30)
                    objs[1] = "音频类";
                else
                    objs[1] = "视频类";
            }
        }
        objs[3] = works.getTitle();
        objs[4] = works.getPoster();
        objs[7] = "";
        if (works.getFileUrl() != null && works.getFileUrl().indexOf(Constants.DIRECTORY_WORKS_FILES) > 0)
            objs[7] = Constants.PUBLISH_DOMAIN + "/" + works.getFileUrl().substring(works.getFileUrl().indexOf(Constants.DIRECTORY_WORKS_FILES));
        objs[8] = works.getMediaLink();

        objs[9] = works.getMediaName();
        objs[10] = works.getSubMediaName();
        objs[11] = works.getMediaPlayDate() == null ? "" : Constants.DFYMD.format(works.getMediaPlayDate());
        objs[12] = works.getMediaPlayTimes();
        objs[13] = works.getProjectBrief();

        objs[15] = works.getVendor();
        return objs;
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
        cell.setCellValue("2022年“上海市健康科普推优选树”参评作品推荐汇总表");

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
    @Override
    public ResponseEntity<byte[]> exportWorksExcel(String poster, Integer type, Integer tjdw) throws IOException {
        //                                0        1       2         3      4     5         6         7        8      9        10            11        12     13     14      15
        String[] headers = new String[]{"机构类型","一级类别","二级类别","标题","作者","申报单位","推荐单位","本地链接", "链接","刊播媒体","刊播版面/栏目","刊播日期","播放量","说明", "手机号", "制作单位"};
        int [] columnWidth = new int[]{  7500,    3000,     6000,    15000,6000, 13000,    13000,   15000,    18000, 11000,    11000,         5000,   3000,  25000,  6000,    5000};
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("作品");
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
        List<Works> works = queryWorks(poster, type, tjdw);
        List<Account> accounts = accountRepository.findAll();
        List<OrgType> orgTypes = orgTypeRepository.findAll();
        List<Dict> dicts = dictRepository.findAll();
        for (int i = 0; i < works.size(); i++) {
            Row row = sheet.createRow(i+2);
            Object[] objs = pickupWorksValue(works.get(i), accounts, orgTypes, dicts, headers.length);
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
        String fileName = "作品导出-" + Constants.DFYMD.format(new Date())+ ".xls";
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

    @Override
    public Works returnWorks(String accountNameame, Integer id) {
        Works w = worksRepository.findById(id).get();
        if (w.getStatus() != Constants.WORKS_STATUS_SUBMIT){
            throw new DataCheckException(HttpStatus.FORBIDDEN, "该记录不在提交状态, 无法回退.");
        }
        w.setStatus(Constants.WORKS_STATUS_DRAFT);
        worksRepository.save(w);
        return w;
    }


    private List<Works> queryWorks(String poster, Integer type, Integer tjdw){
        Specification<Works> spec = (root, query, cb) -> cb.equal(root.get("status"), Constants.WORKS_STATUS_SUBMIT);
        if (poster != null && poster.trim().length() > 0){
            spec.and((Specification<Works>) (root, query, cb) -> cb.like(root.get("poster"), "%" + poster + "%"));
        }
        if (type != null){
            spec.and((Specification<Works>) (root, query, cb) -> cb.equal(root.get("type"), type));
        }
        if (tjdw != null){
            List<Account> accSbdws = accountRepository.findByParentAccountId(tjdw);
            List<Integer> accids = accSbdws.stream().map(a -> a.getId()).collect(Collectors.toList());
            spec.and((Specification<Works>) (root, query, cb) -> root.get("account").get("id").in(accids));
        }
        return worksRepository.findAll(spec);
    }

    /**
     * 控制不让打印数据因为空或者过长导致界面变形
     * @param o
     * @param length
     * @return
     */
    private String formatWorksValue(Object o, int length){
        if (o == null) return "";
        if (o instanceof String) {
            String s = o.toString();
            if (s.length() > length) return s.substring(0, length) + "...";
            return o.toString();
        }
        if (o instanceof Date) return Constants.DFYMD.format(o);
        if (o instanceof Number) return o.toString();
        return "";
    }

    private void getWorksType(Works works, Map<String, String> map){
        map.put("type1", "（  ）图文类 □科普文章 □漫  画  □海报折页 □其他");
        map.put("type2", "（  ）图书类");
        map.put("type3", "（  ）音频类 □专题音频 □广播剧  □有声书   □其他");
        map.put("type4", "（  ）视频类 □单集作品         □系列作品");
        map.put("type5", "           □短视频(≤10分钟)  □长视频(＞10分钟)");
        if (works.getType() < 20){
            if (works.getType() == Constants.WORKS_TYPE_TEXT_SCI)
                map.put("type1", "（✔）图文类 ■科普文章 □漫  画  □海报折页 □其他");
            if (works.getType() == Constants.WORKS_TYPE_TEXT_CARTOON)
                map.put("type1", "（✔）图文类 □科普文章 ■漫  画  □海报折页 □其他");
            if (works.getType() == Constants.WORKS_TYPE_TEXT_POSTER)
                map.put("type1", "（✔）图文类 □科普文章 □漫  画  ■海报折页 □其他");
            if (works.getType() == Constants.WORKS_TYPE_TEXT_OTHERS)
                map.put("type1", "（✔）图文类 □科普文章 □漫  画  □海报折页 ■其他");

        } else if (works.getType() < 30){
            if (works.getType() == Constants.WORKS_TYPE_AUDIO_SPECIAL)
                map.put("type3", "（✔）音频类 ■专题音频 □广播剧  □有声书   □其他");
            if (works.getType() == Constants.WORKS_TYPE_AUDIO_BROADCAST)
                map.put("type3", "（✔）音频类 □专题音频 ■广播剧  □有声书   □其他");
            if (works.getType() == Constants.WORKS_TYPE_AUDIO_BOOK)
                map.put("type3", "（✔）音频类 □专题音频 □广播剧  ■有声书   □其他");
            if (works.getType() == Constants.WORKS_TYPE_AUDIO_OTHERS)
                map.put("type3", "（✔）音频类 □专题音频 □广播剧  □有声书   ■其他");
        } else if (works.getType() < 40){
            if (works.getType() == Constants.WORKS_TYPE_VIDEO_SINGLE_SHORT){
                map.put("type4", "（✔）视频类 ■单集作品         □系列作品");
                map.put("type5", "           ■短视频(≤10分钟)  □长视频(＞10分钟)");
            }
            if (works.getType() == Constants.WORKS_TYPE_VIDEO_SINGLE_LONG){
                map.put("type4", "（✔）视频类 ■单集作品         □系列作品");
                map.put("type5", "           □短视频(≤10分钟)  ■长视频(＞10分钟)");
            }
            if (works.getType() == Constants.WORKS_TYPE_VIDEO_MULTI_SHORT){
                map.put("type4", "（✔）视频类 □单集作品         ■系列作品");
                map.put("type5", "           ■短视频(≤10分钟)  □长视频(＞10分钟)");
            }
            if (works.getType() == Constants.WORKS_TYPE_VIDEO_MULTI_LONG){
                map.put("type4", "（✔）视频类 □单集作品         ■系列作品");
                map.put("type5", "           □短视频(≤10分钟)  ■长视频(＞10分钟)");
            }
        } else if (works.getType() < 50){
            map.put("type2", "（✔）图书类");
        }
    }
}
