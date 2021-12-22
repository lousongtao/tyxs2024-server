package com.jslink.wc.service;

import com.alibaba.fastjson.JSONObject;
import com.jslink.wc.config.DataDict;
import com.jslink.wc.pojo.*;
import com.jslink.wc.repository.*;
import com.jslink.wc.requestbody.AddWorkBody;
import com.jslink.wc.requestbody.MediaBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.responsebody.WorksBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorksServiceImpl implements WorksService{
    @Value("${WorksFileDirectory}")
    private String fileDestDirectory;
    @Autowired
    private WorksRepository worksRepository;
//    @Autowired
//    private WorksCoverRepository worksCoverRepository;
//    @Autowired
//    private WorksAttachmentRepository worksAttachmentRepository;
//    @Autowired
//    private WorksCommentRepository worksCommentRepository;
    @Autowired
    private MediaRepository mediaRepository;
    @Autowired
    private DataDict dataDict;

    /**
     *
     * @param poster
     * @param areaId
     * @param streetId
     * @param page 客户端传入的page值从1开始, 服务端使用0开始, 所以使用时先把page减1
     * @param pageSize
     * @return
     */
    @Override
    public PageResult<WorksBody> getWorks(String poster, Integer areaId, Integer streetId, int page, int pageSize) {
        //order by status desc
        Sort sort = Sort.by("status");
        Pageable pageable = PageRequest.of(page-1, pageSize, sort);
        List<Specification<Works>> specs = new ArrayList<>();
        if (poster != null && poster.trim().length() > 0){
            specs.add((Specification<Works>) (root, query, cb) -> cb.like(root.get("poster"), "%" + poster + "%"));
        }
        if (areaId != null){
            List<Area> areas = dataDict.getAreas();
            List<Area> areas1 = areas.stream().filter(area -> area.getId() == areaId).collect(Collectors.toList());
            if (areas1.isEmpty()) return null;
            List<Integer> streetIds = areas1.get(0).getStreets().stream()
                    .map(street -> street.getId())
                    .collect(Collectors.toList());
            specs.add((Specification<Works>) (root, query, cb) -> root.get("street").in(streetIds));
        }
        if (streetId != null){
            specs.add((Specification<Works>) (root, query, cb) -> cb.equal(root.get("street"), streetId));
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

        List<WorksBody> worksBodies = works.getContent().stream().map(w -> toWorksBody(w)).collect(Collectors.toList());
        //attach the comments
        worksBodies.stream().forEach(w -> attachMedias(w));

        return new PageResult(worksBodies, page, works.getTotalElements());
    }

    /**
     * 给works附加comment信息
     */
//    private void attachComments(WorksBody w){
//        List<WorksComment> comments = worksCommentRepository.findByWorksId(w.getId());
//        //首先把评论信息按照专家分组, stream 中没找到直接按照property取distinct的方式, 只能自己写map
//        Map<String, List<WorksComment>> mapExpertComment = new HashMap<>();
//        for (WorksComment wc: comments){
//            String expert = wc.getExpertName();
//            if (mapExpertComment.get(expert) == null)
//                mapExpertComment.put(expert, new ArrayList<>());
//            mapExpertComment.get(expert).add(wc);
//        }
//        Set<String> expertNames = mapExpertComment.keySet();
//        List<String> dictRateCodes = dataDict.getDicts().stream()
//                .filter(d -> d.getType().equals(Constants.DICT_TYPE_RATE))
//                .map(d -> d.getCode())
//                .collect(Collectors.toList());
//        for(String en : expertNames){
//            List<WorksComment> exComments = mapExpertComment.get(en);
//            JSONObject jo = new JSONObject();
//            for(WorksComment wc: exComments){
//                if (wc.getType().equals(Constants.WORKS_COMMENT_TYPE_COMMENT))
//                    jo.put(Constants.WORKS_COMMENT_TYPE_COMMENT, wc.getValue());
//                if (dictRateCodes.contains(wc.getType()))
//                    jo.put("rate_"+wc.getType(), wc.getValue());
//            }
//            CommentBody commentBody = new CommentBody();
//            commentBody.setExpert(en);
//            commentBody.setComment(jo);
//            w.getComments().add(commentBody);
//        }
//    }

    /**
     * 给works增加播放媒体的信息
     * @param w
     */
    private void attachMedias(WorksBody w){
        List<Media> medias = mediaRepository.findByWorksId(w.getId());
        medias.forEach(media -> {
            MediaBody mb = new MediaBody();
            mb.setLength(media.getLength());
            mb.setName(media.getName());
            mb.setLink(media.getLink());
            mb.setPlayDate(media.getPlayDate());
            mb.setPlayTimes(media.getPlayTimes());
            mb.setSubName(media.getSubName());
            mb.setId(media.getId());
            if (w.getMedias() == null)
                w.setMedias(new ArrayList<>());
            w.getMedias().add(mb);
        });
    }
    /**
     * works db对象转化为response body
     * @param works
     * @return
     */
    private WorksBody toWorksBody(Works works){
        WorksBody wb = new WorksBody();
        wb.setId(works.getId());
//        if (works.getStreet() != null) {
//            wb.setArea(dataDict.getMapArea().get(dataDict.getMapStreetArea().get(works.getStreet())).getName());
//            wb.setStreet(dataDict.getMapStreet().get(works.getStreet()).getName());
//        }
        wb.setIntro(works.getIntro());
        wb.setPhone(works.getPhone());
        wb.setPoster(works.getPoster());
        wb.setType(works.getType());
        wb.setTitle(works.getTitle());
        attachMedias(wb);
//        wb.setStatus(works.getStatus());
//        if (works.getWorksCover() != null)
//            wb.setCoverPath(works.getWorksCover().getFilePath());
//        if (works.getWorksAttachments() != null && works.getWorksAttachments().size() > 0){
//            wb.setAttachmentsPath(works.getWorksAttachments().stream().map(attr -> attr.getFilePath()).collect(Collectors.toList()));
//        }
        return wb;
    }

    /**
     * 保存works的时候, 把用户放在缓存文件夹中的文件拷贝出来, 以新建的works.id为目录. 拷贝后将缓存的目录清空
     * @param body
     * @return
     */
    @Override
    public Works saveWorks(AddWorkBody body) throws IOException {
        Works works = new Works();
        works.setCreateDate(new Date());
        works.setPoster(body.getPoster());
        works.setPhone(body.getPhone());
        works.setTitle(body.getTitle());
        works.setIntro(body.getIntro());
        works.setType(body.getType());
        final Works newWorks = worksRepository.save(works);
        if (body.getMedias() != null){
            body.getMedias().forEach(mb -> {
                Media m = new Media();
                m.setLength(mb.getLength());
                m.setLink(mb.getLink());
                m.setName(mb.getName());
                m.setPlayDate(mb.getPlayDate());
                m.setPlayTimes(mb.getPlayTimes());
                m.setSubName(mb.getSubName());
                m.setWorks(works);
                mediaRepository.save(m);
            });
        }
//        int id = works.getId();
//        if (body.getCover() != null) {
//            String newFile = moveFile(body.getCover(), fileDestDirectory + "/"+id);
//            WorksCover wc = new WorksCover();
//            wc.setWorks(newWorks);
//            wc.setPostDate(new Date());
//            wc.setFilePath(newFile);
//            worksCoverRepository.save(wc);
//        }
//        if (body.getAttachments() != null){
//            for (String att: body.getAttachments()){
//                String newFile = moveFile(att, fileDestDirectory + "/"+id);
//                WorksAttachment wa = new WorksAttachment();
//                wa.setWorks(newWorks);
//                wa.setPostDate(new Date());
//                wa.setFilePath(newFile);
//                worksAttachmentRepository.save(wa);
//            }
//        }
        return newWorks;
    }

    /**
     * 评论功能取消
     * @param user
     * @param json
     * @return
     */
    @Override
    public List<WorksComment> updateWorks(String user, JSONObject json) {
//        Integer id = json.getInteger("id");
//        List<WorksComment> worksComments = new ArrayList<>();
//        final Works works = worksRepository.findById(id).get();
//        works.setStatus(Constants.WORKS_STATUS_AUDIT);
//        worksRepository.save(works);

//        WorksComment wc1 = new WorksComment();
//        wc1.setWorks(works);
//        wc1.setType(Constants.WORKS_COMMENT_TYPE_COMMENT);
//        wc1.setValue(json.getString(Constants.WORKS_COMMENT_TYPE_COMMENT));
//        wc1.setExpertName(user);
//        wc1 = worksCommentRepository.save(wc1);
//        worksComments.add(wc1);
//        List<Dict> dict = dataDict.getDicts();
//        dict = dict.stream().filter(d -> d.getType().equals(Constants.DICT_TYPE_RATE)).collect(Collectors.toList());
//        dict.forEach(d ->{
//            WorksComment wc = new WorksComment();
//            wc.setWorks(works);
//            wc.setType(d.getCode());
//            wc.setValue(json.getString(d.getCode()));
//            wc.setExpertName(user);
//            wc = worksCommentRepository.save(wc);
//            worksComments.add(wc);
//        });
//        return worksComments;
        return null;
    }

    //move file, return the dest file name
    private String moveFile(String src, String destPath) throws IOException {
        File file = new File(src);
        String fileName = file.getName();
        String parent = file.getParent();
        Files.createDirectories(new File(destPath).toPath());
        file.renameTo(new File(destPath + "/" + fileName));
        //文件移动后, 如果目录已空, 就删除目录
        File pFile = new File(parent);
        if (pFile.listFiles() == null || pFile.listFiles().length == 0)
            pFile.delete();
        return destPath + "/" + fileName;
    }


}
