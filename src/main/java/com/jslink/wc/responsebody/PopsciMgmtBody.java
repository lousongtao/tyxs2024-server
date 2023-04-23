package com.jslink.wc.responsebody;

import com.jslink.wc.pojo.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

//科普管理
@Data
public class PopsciMgmtBody {
    private Integer id;
    private Integer applyType;
    private String deptName;
    private String deptAddress;
    private String deptContact;
    private String deptContactDept;
    private String deptMobile;
    private String deptEmail;
    private String name;
    private Integer gender;
    private String race;
    private Date birth;
    private Integer eduDegree;
    private String mobile;
    private String company;
    private String position;
    private String title;
    private String address;
    private String email;
    private String domain;
    private Byte status;
    private String fileUrl;
    private String reccFormFileUrl;//推荐表保存的位置
    private String projectBrief;
    private String projectDesc;
    private List<PopsciIndividualExperience> experienceList;
    private List<PopsciPrize> prizeList;
    private List<PopsciSubsidize> subsidizeList;
}
