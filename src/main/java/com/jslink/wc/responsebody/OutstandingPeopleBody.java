package com.jslink.wc.responsebody;

import com.jslink.wc.pojo.PeopleExperience;
import com.jslink.wc.pojo.PeoplePrize;
import com.jslink.wc.pojo.PeopleSubsidize;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OutstandingPeopleBody {
    private Integer id;
    private Integer applyType;
    private String name;
    private Integer gender;
    private String race;
    private Date birth;
    private Integer eduDegree;
    private String phone;
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
    private List<PeopleExperience> experienceList;
    private List<PeoplePrize> prizeList;
    private List<PeopleSubsidize> subsidizeList;
}
