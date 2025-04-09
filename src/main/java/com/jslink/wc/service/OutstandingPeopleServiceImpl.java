package com.jslink.wc.service;

import com.jslink.wc.exception.DataCheckException;
import com.jslink.wc.exception.NotFoundException;
import com.jslink.wc.pojo.*;
import com.jslink.wc.repository.*;
import com.jslink.wc.requestbody.AddOutstandingPeopleBody;
import com.jslink.wc.requestbody.ExperienceBody;
import com.jslink.wc.requestbody.PrizeBody;
import com.jslink.wc.requestbody.SubsidizeBody;
import com.jslink.wc.responsebody.OutstandingPeopleBody;
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
import org.apache.tomcat.util.bcel.Const;
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
public class OutstandingPeopleServiceImpl implements OutstandingPeopleService{
    @Value("${WorksFileDirectory}")
    private String fileDestDirectory;
    @Value("${template.recommend.people}")
    private String templateRecommend;
    @Value("${WorksReccFormDirectory}")
    private String reccFormDirectory;
    @Autowired
    private OutstandingPeopleRepository peopleRepository;
    @Autowired
    private PeopleExperienceRepository experienceRepository;
    @Autowired
    private PeoplePrizeRepository prizeRepository;
    @Autowired
    private PeopleSubsidizeRepository subsidizeRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private DictService dictService;
    @Autowired
    private OrgTypeRepository orgTypeRepository;
    @Autowired
    private DictRepository dictRepository;
    @Autowired
    private ReturnHistoryRepository returnHistoryRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Override
    public PageResult<OutstandingPeopleBody> getPeople(String accountName, String name, Integer applyType, String phone, String company, int page, int pageSize) {
        //order by status desc
        Sort sort = Sort.by("status");
        Pageable pageable = PageRequest.of(page-1, pageSize, sort);
        List<Specification<OutstandingPeople>> specs = new ArrayList<>();
        if (name != null && name.trim().length() > 0){
            specs.add((Specification<OutstandingPeople>) (root, query, cb) -> cb.like(root.get("name"), "%" + name +"%"));
        }
        if (applyType != null){
            specs.add((Specification<OutstandingPeople>) (root, query, cb) -> cb.equal(root.get("applyType"), applyType));
        }
        if (phone != null && phone.trim().length() > 0){
            specs.add((Specification<OutstandingPeople>) (root, query, cb) -> cb.like(root.get("phone"), "%" + phone +"%"));
        }
        if (company != null && company.trim().length() > 0){
            specs.add((Specification<OutstandingPeople>) (root, query, cb) -> cb.like(root.get("company"), "%" + company + "%"));
        }
        Account account = accountRepository.findByAccount(accountName);
        if (account.getType() == Constants.ACCOUNT_TYPE_UNIT1){
            List<Account> subs = accountRepository.findByParentAccountId(account.getId());
            List<Integer> subIds = subs.stream().map(sa -> sa.getId()).collect(Collectors.toList());
            specs.add((Specification<OutstandingPeople>) (root, query, cb) -> root.get("account").get("id").in(subIds));
        } else if (account.getType() == Constants.ACCOUNT_TYPE_UNIT2){
            specs.add((Specification<OutstandingPeople>) (root, query, cb) -> cb.equal(root.get("account").get("id"), account.getId()));
        }
        if (account.getType() == Constants.ACCOUNT_TYPE_ADMIN || account.getType() == Constants.ACCOUNT_TYPE_UNIT1){
            specs.add((Specification<OutstandingPeople>) (root, query, cb) -> cb.equal(root.get("status"), Constants.WORKS_STATUS_SUBMIT));
        }
        Page<OutstandingPeople> people = null;
        if (specs.isEmpty()){
            people = peopleRepository.findAll(pageable);
        } else {
            Specification<OutstandingPeople> spec = specs.get(0);
            for (int i = 1; i < specs.size(); i++) {
                spec = spec.and(specs.get(i));
            }
            people = peopleRepository.findAll(spec, pageable);
        }

        List<OutstandingPeopleBody> bodies = people.getContent().stream().map(p -> {
            OutstandingPeopleBody body = new OutstandingPeopleBody();
            BeanUtils.copyProperties(p, body);
            body.setExperienceList(experienceRepository.findByPeopleId(p.getId()));
            body.setPrizeList(prizeRepository.findByPeopleId(p.getId()));
            body.setSubsidizeList(subsidizeRepository.findByPeopleId(p.getId()));
            List<ReturnHistory> rhs = returnHistoryRepository.findByObjectIdAndType(p.getId(), Constants.RETURNHISTORY_TYPE_PEOPLE);
            if (!rhs.isEmpty()){
                rhs.sort(Comparator.comparingInt(ReturnHistory::getId));
                body.setReturnHistory(rhs.get(rhs.size() - 1));
            }
            return body;
        }).collect(Collectors.toList());
        return new PageResult<>(bodies, page, people.getTotalElements());
    }

    /**
     * 新建, 对应的经历, 奖励, 资助等, 都用新建操作.
     * @param accountName
     * @param body
     * @return
     */
    @Override
    public OutstandingPeople savePeople(String accountName, AddOutstandingPeopleBody body) throws IOException {
        Account account = accountRepository.findByAccount(accountName);
        OutstandingPeople p = new OutstandingPeople();
        BeanUtils.copyProperties(body, p);
        //前端传递的文件路径, 统一转化为正斜线
        if (body.getFileUrl() != null) {
            String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
            p.setFileUrl(fileUrl);
        }
        p.setAccount(account);
        final OutstandingPeople dbp = peopleRepository.save(p);
        //如果是提交状态, 搬迁文件, 成功后再保存记录一次
        if (body.getStatus() == Constants.WORKS_STATUS_SUBMIT){
            if (body.getFileUrl() != null && body.getFileUrl().length() > 0){
                String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
                String[] segs = fileUrl.split("/");
                String fileName = segs[segs.length - 1];
                String destDir = fileDestDirectory + getPersistFilePath(p);
                String destFile = Utils.moveFile(body.getFileUrl(), destDir);
                p.setFileUrl(destFile);
                p = peopleRepository.save(p);
            }
        }
        createPeopleExpr(dbp, body.getExperienceList());
        createPeoplePrize(dbp, body.getPrizeList());
        createPeopleSubsidize(dbp, body.getSubsidizeList());

        return dbp;
    }

    private void createPeopleExpr(OutstandingPeople people, List<ExperienceBody> exprs){
        if (exprs != null){
            exprs.stream().forEach(expr -> {
                PeopleExperience pe = new PeopleExperience();
                BeanUtils.copyProperties(expr, pe);
                pe.setPeople(people);
                experienceRepository.save(pe);
            });
        }
    }

    private void createPeoplePrize(OutstandingPeople people, List<PrizeBody> prizes){
        if (prizes != null){
            prizes.stream().forEach(prize -> {
                PeoplePrize pp = new PeoplePrize();
                BeanUtils.copyProperties(prize, pp);
                pp.setPeople(people);
                prizeRepository.save(pp);
            });
        }
    }

    private void createPeopleSubsidize(OutstandingPeople people, List<SubsidizeBody> subsidizes){
        if (subsidizes != null){
            subsidizes.stream().forEach(subsidize -> {
                PeopleSubsidize ps = new PeopleSubsidize();
                BeanUtils.copyProperties(subsidize, ps);
                ps.setPeople(people);
                subsidizeRepository.save(ps);
            });
        }
    }

    /**
     * 修改people, 对于下属的经历等子项, 先全部删除, 再全部新建.
     * @param accountName
     * @return
     */
    @Override
    public OutstandingPeople updatePeople(String accountName, Integer id, AddOutstandingPeopleBody body) throws IOException {
        OutstandingPeople people = peopleRepository.findById(id).get();
        BeanUtils.copyProperties(body, people, "status");
        //前端传递的文件路径, 统一转化为正斜线
        people.setFileUrl(null);
        if (body.getFileUrl() != null) {
            String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
            people.setFileUrl(fileUrl);
        }
        if (people.getStatus() == Constants.WORKS_STATUS_DRAFT && body.getStatus() == Constants.WORKS_STATUS_SUBMIT){
            people.setStatus(Constants.WORKS_STATUS_SUBMIT);
            if (body.getFileUrl() != null && body.getFileUrl().length() > 0){
                String fileUrl = body.getFileUrl().replaceAll("\\\\", "/");
                String[] segs = fileUrl.split("/");
                String fileName = segs[segs.length - 1];
                String destDir = fileDestDirectory + getPersistFilePath(people);
                String destFile = Utils.moveFile(body.getFileUrl() , destDir);
                people.setFileUrl(destFile);
            }
        }
        final OutstandingPeople dbp = peopleRepository.save(people);
        prizeRepository.deleteByPeopleId(id);
        experienceRepository.deleteByPeopleId(id);
        subsidizeRepository.deleteByPeopleId(id);
        createPeopleExpr(dbp, body.getExperienceList());
        createPeoplePrize(dbp, body.getPrizeList());
        createPeopleSubsidize(dbp, body.getSubsidizeList());
        return dbp;
    }

    @Override
    public OutstandingPeople returnPeople(String accountName, Integer id, String returnReason) {
        OutstandingPeople people = peopleRepository.findById(id).get();
        if (people.getStatus() != Constants.WORKS_STATUS_SUBMIT){
            throw new DataCheckException(HttpStatus.FORBIDDEN, "该记录不在提交状态, 无法回退.");
        }
        people.setStatus(Constants.WORKS_STATUS_DRAFT);
        peopleRepository.save(people);
        ReturnHistory rh = new ReturnHistory(Constants.RETURNHISTORY_TYPE_PEOPLE, returnReason, id, new Date());
        returnHistoryRepository.save(rh);
        entityManager.flush();//测试中发现对象创建后未持久化到数据库, 虽然在当前session下能查到这个数据, 但是不保证该数据合适进行持久化, 为了安全, 这里手动持久化.
        return people;
    }

    @Override
    public ResponseEntity<byte[]> printPeople(Integer id) throws UnsupportedEncodingException {
        Optional<OutstandingPeople> optPeople = peopleRepository.findById(id);
        if (!optPeople.isPresent()) throw new NotFoundException("未找到数据, id="+id);
        OutstandingPeople people = optPeople.get();
        Account postAccount = people.getAccount();
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
        if (people.getApplyType() == Constants.PEOPLE_APPLYTYPE_GONGXIAN)
            map.put("applyType", "■特别贡献人物 □杰出人物  □新锐人物");
        if (people.getApplyType() == Constants.PEOPLE_APPLYTYPE_JIECU)
            map.put("applyType", "□特别贡献人物 ■杰出人物  □新锐人物");
        if (people.getApplyType() == Constants.PEOPLE_APPLYTYPE_XINRUI)
            map.put("applyType", "□特别贡献人物 □杰出人物  ■新锐人物");
        map.put("name", people.getName());
        map.put("gender", people.getGender() == Constants.GENDER_MALE ? "男" : (people.getGender() == Constants.GENDER_FEMALE ? "女" : ""));
        map.put("race", people.getRace());
        map.put("birth", formatPrintValue(people.getBirth(), 0));
        map.put("eduDegree", dictService.getDict("degree", people.getEduDegree()));
        map.put("phone", people.getPhone());
        map.put("company", people.getCompany());
        map.put("position", people.getPosition());
        map.put("title", people.getTitle());
        map.put("address", people.getAddress());
        map.put("email", people.getEmail());
        map.put("domain", people.getDomain());
        map.put("projectBrief", people.getProjectBrief());
        map.put("projectDesc", people.getProjectDesc());
        String orgType = null;
        if (reccAccount.getOrgTypeId() != null) {
            OrgType ot = orgTypeRepository.findById(reccAccount.getOrgTypeId()).get();
            orgType = ot.getName();
        }
        map.put("tjdw_tag", formatPrintValue(orgType, 2000));
        map.put("tjdw", formatPrintValue(reccAccount.getName(), 1800));
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

        List<PeoplePrize> prizeList = prizeRepository.findByPeopleId(id);
        Table table = Utils.findTableByKeyword(document, "曾获科普奖励情况");
        Utils.addPrizeRow(table, prizeList);

        List<PeopleSubsidize> subsidizeList = subsidizeRepository.findByPeopleId(id);
        Table tableSub = Utils.findTableByKeyword(document, "曾获计划资助情况");
        Utils.addSubsidizeRow(tableSub, subsidizeList);

        List<PeopleExperience> experienceList = experienceRepository.findByPeopleId(id);
        Table tableExp = Utils.findTableByKeyword(document, "工作经历");
        Utils.addExperienceRow(tableExp, experienceList);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        document.saveToStream(os, FileFormat.Doc);
        String fileName = postAccount.getName() + "-" + people.getName() + "-" + people.getId() + ".doc";
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

    @Override
    public String UploadReccForm(MultipartFile file, int id) throws IOException {
        if (file.isEmpty()){
            throw new IOException("Empty file exception");
        }
        //如果之前曾上传过推荐表, 先把原文件删除.
        OutstandingPeople people = peopleRepository.findById(id).get();
        if (people.getReccFormFileUrl() != null){
            File old = new File(people.getReccFormFileUrl());
            old.delete();
        }
        //中文文件名在Linux上报错, 这里统一改成reccform
        String[] segs = file.getOriginalFilename().split("\\.");
        String newName = "reccform."+segs[segs.length - 1];
        Path target = Paths.get(fileDestDirectory + getPersistFilePath(people) + "/" + newName);
        //如果该文件已存在, 就修改文件名, 否则会出现重名文件冲突
        int i = 0;
        while(target.toFile() != null && target.toFile().exists()){
            newName = "reccform(" + (++i) + ")."+segs[segs.length - 1];
            target = Paths.get(fileDestDirectory + getPersistFilePath(people) + "/" + newName);
        }
        Files.createDirectories(target.getParent());
        Files.createFile(target);
        try(InputStream is = file.getInputStream()){
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }
        String path = target.toFile().getAbsolutePath();
        //前端传递的文件路径, 统一转化为正斜线
        path = path.replaceAll("\\\\", "/");
        people.setReccFormFileUrl(path);
        peopleRepository.save(people);
        return path;
    }

    /**
     * 根据2024年的要求, 将每个作品放入到一个文件夹下, 该文件夹下包括作品和推荐表
     * 需要考虑到某些特殊符号无法生成目录名, 需要做一些转换, 比如把英文符号转化成中文符号
     * @return
     */
    private String getPersistFilePath(OutstandingPeople people){
//        Account sbdw = people.getAccount();
//        Account tjdw = accountRepository.findById(sbdw.getParentAccountId()).get();
//        OrgType ot = orgTypeRepository.findById(tjdw.getOrgTypeId()).get();
//        return "/".concat(ot.getName()).concat("/").concat(tjdw.getName()).concat("/").concat(sbdw.getName())
//                .concat("/科普人物/").concat(people.getId().toString());
        String dir1 = "/科普新锐人物/";
        if (people.getApplyType() == Constants.PEOPLE_APPLYTYPE_JIECU)
            dir1 = "/科普杰出人物/";
        return dir1.concat(Utils.removeSpecialChars(people.getName()));
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
        cell.setCellValue("2024年“上海市健康科普推优选树”科普人物推荐汇总表");

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

    private Object[] pickupExportValue(OutstandingPeople people, List<Account> accounts, List<OrgType> orgTypes, List<Dict> dicts, int length){
        Object[] objs = new Object[length];
        Account postAcc = people.getAccount();
        if (postAcc != null){
            objs[2] = postAcc.getName();
            objs[20] = postAcc.getPhone();
            for(Account tj: accounts){
                if (tj.getId().equals(postAcc.getParentAccountId())){
                    objs[1] = tj.getName();
                    for(OrgType ot : orgTypes){
                        if (ot.getId().equals(tj.getOrgTypeId()))
                            objs[0] = ot.getName();
                    }
                    objs[18] = tj.getContactPerson();
                    objs[19] = tj.getPhone();
                    break;
                }
            }
        }
        for(Dict d: dicts){
            if (d.getType().equals(Constants.POPSCI_APPLYTYPE) && d.getValue().equals(people.getApplyType())) {
                objs[4] = d.getName();
            }
            if (d.getType().equals(Constants.DEGREE) && d.getValue().equals(people.getEduDegree())){
                objs[10] = d.getName();
            }
        }
        if (people.getFileUrl() != null && people.getFileUrl().indexOf(Constants.DIRECTORY_WORKS_FILES) > 0)
            objs[3] = Constants.PUBLISH_DOMAIN + "/" + people.getFileUrl().substring(people.getFileUrl().indexOf(Constants.DIRECTORY_WORKS_FILES));
        objs[5] = people.getApplyType() == 3 ? "新锐人物" : "杰出人物";
        objs[6] = people.getName();
        objs[7] = people.getGender() == null ? "" : (people.getGender() == Constants.GENDER_MALE ? "男" : "女");
        objs[8] = people.getRace();
        objs[9] = people.getBirth() == null ? "" : Constants.DFYMD.format(people.getBirth());
        objs[11] = people.getPhone();
        objs[12] = people.getCompany();
        objs[13] = people.getPosition();
        objs[14] = people.getTitle();
        objs[15] = people.getAddress();
        objs[16] = people.getEmail();
        objs[17] = people.getDomain();

        return objs;
    }

    @Override
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        //                                0        1          2         3          4         5          6      7    8        9       10         11      12   13     14       15        16           17                18              19             20
        String[] headers = new String[]{"机构类型", "推荐单位", "申报单位", "本地链接", "申报类别", "申报类别", "姓名", "性别","民族","出生年月","学历","手机号码","工作单位", "职务", "职称", "通讯地址", "电子邮箱", "从事专业/工作领域", "推荐单位联系人", "推荐单位联系电话", "提交人联系电话"};
        int [] columnWidth = new int[]{  7000,     7000,      7000,     13000,     3000,     5000,     5000,   2000, 2000, 5000,    4000,   5000,   10000,    6000,  6000,   13000,    10000,     10000,             10000,              10000,       10000};
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("科普人物");
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
        List<OutstandingPeople> people = peopleRepository.findByStatus(Constants.WORKS_STATUS_SUBMIT);
        List<Account> accounts = accountRepository.findAll();
        List<OrgType> orgTypes = orgTypeRepository.findAll();
        List<Dict> dicts = dictRepository.findAll();
        for (int i = 0; i < people.size(); i++) {
            Row row = sheet.createRow(i+2);
            Object[] objs = pickupExportValue(people.get(i), accounts, orgTypes, dicts, headers.length);
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
        String fileName = "科普人物导出-" + Constants.DFYMD.format(new Date())+ ".xls";
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
