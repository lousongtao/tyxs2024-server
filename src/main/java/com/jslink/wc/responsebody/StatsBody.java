package com.jslink.wc.responsebody;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jslink.wc.pojo.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class StatsBody {
    private Integer row;
    private String type;
    private Integer tjdwCount;
    private Integer sbdwCount;
    private Integer brandCount;
    private Integer mgmtPersonCount;
    private Integer mgmtOrgCount;
    private Integer worksCount;
    private Integer peopleCount;
    private List<AccountBody> tjdws;
    private List<AccountBody> sbdws;
    private List<BrandBody> brands;
    private List<PopsciMgmtBody> mgmtOrgs;
    private List<PopsciMgmtBody> mgmtPersons;
    private List<WorksBody> works;
    private List<OutstandingPeopleBody> people;

    public StatsBody(Integer row, String type) {
        this.row = row;
        this.type = type;
    }

    public void setTjdws(List<AccountBody> tjdws) {
        this.tjdws = tjdws;
        setTjdwCount(tjdws.size());
    }

    public void setSbdws(List<AccountBody> sbdws) {
        this.sbdws = sbdws;
        setSbdwCount(sbdws.size());
    }

    public void setBrands(List<BrandBody> brands) {
        this.brands = brands;
        setBrandCount(brands.size());
    }

    public void setMgmtOrgs(List<PopsciMgmtBody> mgmtOrgs) {
        this.mgmtOrgs = mgmtOrgs;
        setMgmtOrgCount(mgmtOrgs.size());
    }

    public void setMgmtPersons(List<PopsciMgmtBody> mgmtPersons) {
        this.mgmtPersons = mgmtPersons;
        setMgmtPersonCount(mgmtPersons.size());
    }

    public void setWorks(List<WorksBody> works) {
        this.works = works;
        setWorksCount(works.size());
    }

    public void setPeople(List<OutstandingPeopleBody> people) {
        this.people = people;
        setPeopleCount(people.size());
    }
}
