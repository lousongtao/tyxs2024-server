package com.jslink.wc.requestbody;

import lombok.Data;

import javax.persistence.Column;
import java.util.List;

@Data
public class AddBrandBody {
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
    private List<PrizeBody> prizeList;
    private List<SubsidizeBody> subsidizeList;
    private Byte status;
    private String fileUrl;
    private int quantityBrand;
    private int quantityPeople;
    private int quantityMgmtIndividual;
    private int quantityMgmtOrg;
    private int quantityWorks;
}
