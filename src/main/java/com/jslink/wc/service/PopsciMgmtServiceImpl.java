package com.jslink.wc.service;

import com.jslink.wc.exception.DataCheckException;
import com.jslink.wc.exception.NotFoundException;
import com.jslink.wc.pojo.*;
import com.jslink.wc.repository.*;
import com.jslink.wc.requestbody.AddPopsciMgmtBody;
import com.jslink.wc.requestbody.ExperienceBody;
import com.jslink.wc.requestbody.PrizeBody;
import com.jslink.wc.requestbody.SubsidizeBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.responsebody.PopsciMgmtBody;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
public class PopsciMgmtServiceImpl implements PopsciMgmtService{
    @Value("${template.recommend.mgmt}")
    private String templateRecommend;
    @Value("${WorksFileDirectory}")
    private String fileDestDirectory;
    @Value("${WorksReccFormDirectory}")
    private String reccFormDirectory;
    @Autowired
    private PopsciMgmtRepository popsciMgmtRepository;
    @Autowired
    private PopsciPrizeRepository prizeRepository;
    @Autowired
    private PopsciSubsidizeRepository subsidizeRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private DictService dictService;
    @Autowired
    private OrgTypeRepository orgTypeRepository;
    @Autowired
    private DictRepository dictRepository;
    @Autowired
    private PopsciIndividualExperienceRepository popsciIndividualExperienceRepository;
    @Autowired
    private ReturnHistoryRepository returnHistoryRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Override
    public PageResult<PopsciMgmtBody> getPopsci(String accountName, Integer applyType, String deptName, String name, int currentPage, int pageSize) {
        //order by status desc
        Sort sort = Sort.by("status");
        Pageable pageable = PageRequest.of(currentPage-1, pageSize, sort);
        List<Specification<PopsciMgmt>> specs = new ArrayList<>();
//        if (name != null && name.trim().length() > 0){
//            specs.add((Specification<PopsciMgmt>) (root, query, cb) -> cb.like(root.get("name"), "%" + name +"%"));
//        }
        Account account = accountRepository.findByAccount(accountName);
        if (account.getType() == Constants.ACCOUNT_TYPE_UNIT1){
            List<Account> subs = accountRepository.findByParentAccountId(account.getId());
            List<Integer> subIds = subs.stream().map(sa -> sa.getId()).collect(Collectors.toList());
            specs.add((Specification<PopsciMgmt>) (root, query, cb) -> root.get("account").get("id").in(subIds));
        } else if (account.getType() == Constants.ACCOUNT_TYPE_UNIT2){
            specs.add((Specification<PopsciMgmt>) (root, query, cb) -> cb.equal(root.get("account").get("id"), account.getId()));
        }
        if (account.getType() == Constants.ACCOUNT_TYPE_ADMIN || account.getType() == Constants.ACCOUNT_TYPE_UNIT1){
            specs.add((Specification<PopsciMgmt>) (root, query, cb) -> cb.equal(root.get("status"), Constants.WORKS_STATUS_SUBMIT));
        }
        if (applyType != null){
            specs.add((Specification<PopsciMgmt>) (root, query, cb) -> cb.equal(root.get("applyType"), applyType));
        }
        if (deptName != null && deptName.trim().length() > 0){
            specs.add((Specification<PopsciMgmt>) (root, query, cb) -> cb.like(root.get("deptName"), "%" + deptName + "%"));
        }
        if (name != null && name.trim().length() > 0){
            specs.add((Specification<PopsciMgmt>) (root, query, cb) -> cb.like(root.get("name"), "%" + name + "%"));
        }
        Page<PopsciMgmt> pcs;
        if (specs.isEmpty()){
            pcs = popsciMgmtRepository.findAll(pageable);
        } else {
            Specification<PopsciMgmt> spec = specs.get(0);
            for (int i = 1; i < specs.size(); i++) {
                spec = spec.and(specs.get(i));
            }
            pcs = popsciMgmtRepository.findAll(spec, pageable);
        }

        List<PopsciMgmtBody> bodies = pcs.getContent().stream().map(b -> {
            PopsciMgmtBody body = new PopsciMgmtBody();
            BeanUtils.copyProperties(b, body);
            body.setSubsidizeList(subsidizeRepository.findByPopsciMgmtId(b.getId()));
            body.setPrizeList(prizeRepository.findByPopsciMgmtId(b.getId()));
            body.setExperienceList(popsciIndividualExperienceRepository.findByPopsciMgmtId(b.getId()));
            List<ReturnHistory> rhs = returnHistoryRepository.findByObjectIdAndType(b.getId(), Constants.RETURNHISTORY_TYPE_POPSCI);
            if (!rhs.isEmpty()){
                rhs.sort(Comparator.comparingInt(ReturnHistory::getId));
                body.setReturnHistory(rhs.get(rhs.size() - 1));
            }
            return body;
        }).collect(Collectors.toList());
        return new PageResult<>(bodies, currentPage, pcs.getTotalElements());
    }

    @Override
    public PopsciMgmt returnPopsci(String accountName, Integer id, String returnReason) {
        PopsciMgmt ps = popsciMgmtRepository.findById(id).get();
        if (ps.getStatus() != Constants.WORKS_STATUS_SUBMIT){
            throw new DataCheckException(HttpStatus.FORBIDDEN, "该记录不在提交状态, 无法回退.");
        }
        ps.setStatus(Constants.WORKS_STATUS_DRAFT);
        popsciMgmtRepository.save(ps);
        ReturnHistory rh = new ReturnHistory(Constants.RETURNHISTORY_TYPE_POPSCI, returnReason, id, new Date());
        returnHistoryRepository.save(rh);
        entityManager.flush();//测试中发现对象创建后未持久化到数据库, 虽然在当前session下能查到这个数据, 但是不保证该数据合适进行持久化, 为了安全, 这里手动持久化.
        return ps;
    }

    @Override
    public ResponseEntity<byte[]> printPopsci(Integer id) throws UnsupportedEncodingException {
        Optional<PopsciMgmt> optPopsciMgmt = popsciMgmtRepository.findById(id);
        if (!optPopsciMgmt.isPresent()) throw new NotFoundException("未找到数据, id="+id);
        PopsciMgmt popsciMgmt = optPopsciMgmt.get();
        Account postAccount = popsciMgmt.getAccount();
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

        String fileName = null;
        Map<String, String> map = new HashMap<>();
        if (popsciMgmt.getApplyType() == Constants.PEOPLE_APPLYTYPE_GONGXIAN){
            fileName = postAccount.getName() + "-" + popsciMgmt.getDeptName() + "-" + popsciMgmt.getId() + ".doc";
            map.put("applyType", "■机构组织 □个人");
            map.put("deptName", popsciMgmt.getDeptName());
            map.put("deptAddress", popsciMgmt.getDeptAddress());
            map.put("deptContact", popsciMgmt.getDeptContact());
            map.put("deptContactDept", popsciMgmt.getDeptContactDept());
            map.put("deptMobile", popsciMgmt.getDeptMobile());
            map.put("deptEmail", popsciMgmt.getDeptEmail());

            map.put("name", "");
            map.put("gender", "");
            map.put("race", "");
            map.put("birth", "");
            map.put("eduDegree", "");
            map.put("mobile", "");
            map.put("company", "");
            map.put("position", "");
            map.put("title", "");
            map.put("address", "");
            map.put("email", "");
            map.put("domain", "");
        }

        if (popsciMgmt.getApplyType() == Constants.PEOPLE_APPLYTYPE_JIECU) {
            fileName = postAccount.getName() + "-" + popsciMgmt.getName() + "-" + popsciMgmt.getId() + ".doc";
            map.put("applyType", "□机构组织 ■个人");
            map.put("deptName", "");
            map.put("deptAddress", "");
            map.put("deptContact", "");
            map.put("deptContactDept", "");
            map.put("deptMobile", "");
            map.put("deptEmail", "");

            map.put("name", popsciMgmt.getName());
            map.put("gender", popsciMgmt.getGender() == Constants.GENDER_MALE ? "男" : (popsciMgmt.getGender() == Constants.GENDER_FEMALE ? "女" : ""));
            map.put("race", popsciMgmt.getRace());
            map.put("birth", formatPrintValue(popsciMgmt.getBirth(), 0));
            map.put("eduDegree", dictService.getDict("degree", popsciMgmt.getEduDegree()));
            map.put("mobile", popsciMgmt.getMobile());
            map.put("company", popsciMgmt.getCompany());
            map.put("position", popsciMgmt.getPosition());
            map.put("title", popsciMgmt.getTitle());
            map.put("address", popsciMgmt.getAddress());
            map.put("email", popsciMgmt.getEmail());
            map.put("domain", popsciMgmt.getDomain());
        }
        map.put("projectBrief", popsciMgmt.getProjectBrief());
        map.put("projectDesc", popsciMgmt.getProjectDesc());

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

        Table table = Utils.findTableByKeyword(document, "曾获科普奖励情况");
        List<PopsciPrize> prizeList = prizeRepository.findByPopsciMgmtId(id);
        Utils.addPrizeRow(table, prizeList);

        List<PopsciSubsidize> subsidizeList = subsidizeRepository.findByPopsciMgmtId(id);
        Table tableSub = Utils.findTableByKeyword(document, "曾获计划资助情况");
        Utils.addSubsidizeRow(tableSub, subsidizeList);

        List<PopsciIndividualExperience> experienceList = popsciIndividualExperienceRepository.findByPopsciMgmtId(id);
        Table tableExp = Utils.findTableByKeyword(document, "工作经历");
        Utils.addExperienceRow(tableExp, experienceList);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        document.saveToStream(os, FileFormat.Doc);
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
        PopsciMgmt popsciMgmt = popsciMgmtRepository.findById(id).get();
        if (popsciMgmt.getReccFormFileUrl() != null){
            File old = new File(popsciMgmt.getReccFormFileUrl());
            old.deleteOnExit();
        }
        //中文文件名在Linux上报错, 这里统一改成reccform
        String[] segs = file.getOriginalFilename().split("\\.");
        String newName = "reccform."+segs[segs.length - 1];
        Path target = Paths.get(reccFormDirectory + getPersistFilePath(popsciMgmt) + "/" + id + "/" + newName);
        //如果该文件已存在, 就修改文件名, 否则会出现重名文件冲突
        int i = 0;
        while(target.toFile() != null && target.toFile().exists()){
            newName = "reccform(" + (++i) + ")."+segs[segs.length - 1];
            target = Paths.get(reccFormDirectory + getPersistFilePath(popsciMgmt) + "/" + id + "/" + newName);
        }
        Files.createDirectories(target.getParent());
        Files.createFile(target);
        try(InputStream is = file.getInputStream()){
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }
        String path = target.toFile().getAbsolutePath();
        //前端传递的文件路径, 统一转化为正斜线
        path = path.replaceAll("\\\\", "/");
        popsciMgmt.setReccFormFileUrl(path);
        popsciMgmtRepository.save(popsciMgmt);
        return path;
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
        cell.setCellValue("2022年“上海市健康科普推优选树”科普管理推荐汇总表");

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

    private Object[] pickupExportValue(PopsciMgmt mgmt, List<Account> accounts, List<OrgType> orgTypes, List<Dict> dicts, int length){
        Object[] objs = new Object[length];
        Account postAcc = mgmt.getAccount();
        if (postAcc != null){
            objs[2] = postAcc.getName();
            for(Account tj: accounts){
                if (tj.getId().equals(postAcc.getParentAccountId())){
                    objs[1] = tj.getName();
                    for(OrgType ot : orgTypes){
                        if (ot.getId().equals(tj.getOrgTypeId()))
                            objs[0] = ot.getName();
                    }
                    objs[23] = "";
                    objs[24] = "";
                    if (mgmt.getApplyType() == Constants.POPSCI_APPLYTYPE_INDIVIDUAL){
                        objs[23] = tj.getContactPerson();
                        objs[24] = tj.getPhone();
                    }
                    break;
                }
            }
        }
        for(Dict d: dicts){
            if (d.getType().equals(Constants.POPSCI_APPLYTYPE) && d.getValue().equals(mgmt.getApplyType())) {
                objs[4] = d.getName();
            }
            if (d.getType().equals(Constants.DEGREE) && d.getValue().equals(mgmt.getEduDegree())){
                objs[15] = d.getName();
            }
        }
        if (mgmt.getFileUrl() != null && mgmt.getFileUrl().indexOf(Constants.DIRECTORY_WORKS_FILES) > 0)
            objs[3] = Constants.PUBLISH_DOMAIN + "/" + mgmt.getFileUrl().substring(mgmt.getFileUrl().indexOf(Constants.DIRECTORY_WORKS_FILES));
        objs[5] = mgmt.getDeptName();
        objs[6] = mgmt.getDeptAddress();
        objs[7] = mgmt.getDeptContact();
        objs[8] = mgmt.getDeptContactDept();
        objs[9] = mgmt.getDeptMobile();
        objs[10] = mgmt.getDeptEmail();
        objs[11] = mgmt.getName();
        objs[12] = mgmt.getGender() == null ? "" : (mgmt.getGender() == Constants.GENDER_MALE ? "男" : "女");
        objs[13] = mgmt.getRace();
        objs[14] = mgmt.getBirth() == null ? "" : Constants.DFYMD.format(mgmt.getBirth());
        objs[16] = mgmt.getMobile();
        objs[17] = mgmt.getCompany();
        objs[18] = mgmt.getPosition();
        objs[19] = mgmt.getTitle();
        objs[20] = mgmt.getAddress();
        objs[21] = mgmt.getEmail();
        objs[22] = mgmt.getDomain();

        return objs;
    }

    @Override
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        //                                0        1          2         3          4         5       6         7        8        9       10         11      12   13     14       15       16     17        18     19     20         21        22                23               24
        String[] headers = new String[]{"机构类型", "推荐单位", "申报单位", "本地链接", "申报类别","单位名称","通讯地址","联系人","联系部门","手机号码","电子邮箱","姓名", "性别","民族","出生年月","学历","手机号码","工作单位", "职务", "职称", "通讯地址", "电子邮箱", "从事专业/工作领域", "推荐单位联系人", "推荐单位联系电话"};
        int [] columnWidth = new int[]{  7000,     7000,      7000,     13000,     3000,    8000,     12000,   4000,   6000,     5000,    10000,   5000,    2000, 2000, 5000,    4000,   5000,   10000,    6000,  6000,   13000,    10000,     10000,            10000,           10000};
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("科普管理");
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
        List<PopsciMgmt> mgmts = popsciMgmtRepository.findByStatus(Constants.WORKS_STATUS_SUBMIT);
        List<Account> accounts = accountRepository.findAll();
        List<OrgType> orgTypes = orgTypeRepository.findAll();
        List<Dict> dicts = dictRepository.findAll();
        for (int i = 0; i < mgmts.size(); i++) {
            Row row = sheet.createRow(i+2);
            Object[] objs = pickupExportValue(mgmts.get(i), accounts, orgTypes, dicts, headers.length);
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
        String fileName = "科普管理导出-" + Constants.DFYMD.format(new Date())+ ".xls";
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

    private String getPersistFilePath(PopsciMgmt popsciMgmt){
        Account sbdw = popsciMgmt.getAccount();
        Account tjdw = accountRepository.findById(sbdw.getParentAccountId()).get();
        OrgType ot = orgTypeRepository.findById(tjdw.getOrgTypeId()).get();
        return "/".concat(ot.getName()).concat("/").concat(tjdw.getName()).concat("/").concat(sbdw.getName())
                .concat("/科普管理/").concat(popsciMgmt.getId().toString());
    }

    /**
     * 控制不让打印数据因为空或者过长导致界面变形
     * @param o
     * @param length
     * @return
     */
    private String formatPrintValue(Object o, int length){
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

    /**
     * 新建, 对应的经历, 奖励, 资助等, 都用新建操作.
     * @param accountName
     * @param body
     * @return
     */
    @Override
    public PopsciMgmt save(String accountName, AddPopsciMgmtBody body) throws IOException {
        Account account = accountRepository.findByAccount(accountName);
        PopsciMgmt psm = new PopsciMgmt();
        BeanUtils.copyProperties(body, psm);
        //前端传递的文件路径, 统一转化为正斜线
        if (body.getFileUrl() != null) {
            String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
            psm.setFileUrl(fileUrl);
        }
        psm.setAccount(account);
        psm = popsciMgmtRepository.save(psm);
        //如果是提交状态, 搬迁文件, 成功后再保存记录一次
        if (body.getStatus() == Constants.WORKS_STATUS_SUBMIT){
            if (body.getFileUrl() != null && body.getFileUrl().length() > 0){
                String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
                String[] segs = fileUrl.split("/");
                String fileName = segs[segs.length - 1];
                String destDir = fileDestDirectory + getPersistFilePath(psm);
                String destFile = Utils.moveFile(body.getFileUrl(), destDir);
                psm.setFileUrl(destFile);
                psm = popsciMgmtRepository.save(psm);
            }
        }
        createPeopleExpr(psm, body.getExperienceList());
        createPopsciMgmtPrize(psm, body.getPrizeList());
        createPopsciMgmtSubsidize(psm, body.getSubsidizeList());
        return psm;
    }

    /**
     * 修改, 对于下属的经历等子项, 先全部删除, 再全部新建.
     * @param accountName
     * @param id
     * @param body
     * @return
     */
    @Override
    public PopsciMgmt update(String accountName, Integer id, AddPopsciMgmtBody body) throws IOException {
        PopsciMgmt psm = popsciMgmtRepository.findById(id).get();
        BeanUtils.copyProperties(body, psm, "status");
        //前端传递的文件路径, 统一转化为正斜线
        psm.setFileUrl(null);
        if (body.getFileUrl() != null) {
            String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
            psm.setFileUrl(fileUrl);
        }
        if (psm.getStatus() == Constants.WORKS_STATUS_DRAFT && body.getStatus() == Constants.WORKS_STATUS_SUBMIT){
            psm.setStatus(Constants.WORKS_STATUS_SUBMIT);
            if (body.getFileUrl() != null && body.getFileUrl().length() > 0){
                String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
                String[] segs = fileUrl.split("/");
                String fileName = segs[segs.length - 1];
                String destDir = fileDestDirectory + getPersistFilePath(psm);
                String destFile = Utils.moveFile(body.getFileUrl() , destDir);
                psm.setFileUrl(destFile);
            }
        }
        psm = popsciMgmtRepository.save(psm);
        prizeRepository.deleteByPopsciMgmtId(id);
        subsidizeRepository.deleteByPopsciMgmtId(id);
        popsciIndividualExperienceRepository.deleteByPopsciMgmtId(id);
        createPeopleExpr(psm, body.getExperienceList());
        createPopsciMgmtPrize(psm, body.getPrizeList());
        createPopsciMgmtSubsidize(psm, body.getSubsidizeList());
        return psm;
    }

    private void createPeopleExpr(PopsciMgmt psm, List<ExperienceBody> exprs){
        if (exprs != null){
            exprs.stream().forEach(expr -> {
                PopsciIndividualExperience pe = new PopsciIndividualExperience();
                BeanUtils.copyProperties(expr, pe);
                pe.setPopsciMgmt(psm);
                popsciIndividualExperienceRepository.save(pe);
            });
        }
    }

    private void createPopsciMgmtPrize(PopsciMgmt psm, List<PrizeBody> prizes){
        if (prizes != null){
            prizes.stream().forEach(prize -> {
                PopsciPrize pp = new PopsciPrize();
                BeanUtils.copyProperties(prize, pp);
                pp.setPopsciMgmt(psm);
                prizeRepository.save(pp);
            });
        }
    }

    private void createPopsciMgmtSubsidize(PopsciMgmt psm, List<SubsidizeBody> subsidizes){
        if (subsidizes != null){
            subsidizes.stream().forEach(subsidize -> {
                PopsciSubsidize ps = new PopsciSubsidize();
                BeanUtils.copyProperties(subsidize, ps);
                ps.setPopsciMgmt(psm);
                subsidizeRepository.save(ps);
            });
        }
    }


}
