package com.jslink.wc.responsebody;

import com.jslink.wc.pojo.BrandPrize;
import com.jslink.wc.pojo.BrandSubsidize;
import lombok.Data;

import java.util.List;

@Data
public class BrandBody {
    private Integer id;
    private String name;
    private String type;
    private String category;
    private String company;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String projectBrief;
    private String projectDesc;
    private Byte status;
    private String fileUrl;
    private String reccFormFileUrl;//推荐表保存的位置
    private List<BrandPrize> prizeList;
    private List<BrandSubsidize> subsidizeList;
}
