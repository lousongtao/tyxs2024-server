package com.jslink.wc.pojo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;
@Data
@Table
@Entity
public class ReturnHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    private Integer type;//1-brand; 2-people; 3-popsci;4-works
    @Column
    private String reason;
    @Column
    private Integer objectId;
    @Column
    private Date time;

    public ReturnHistory() {
    }

    public ReturnHistory(Integer type, String reason, Integer objectId, Date time) {
        this.type = type;
        this.reason = reason;
        this.objectId = objectId;
        this.time = time;
    }

    public Integer getId() {
        return id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getObjectId() {
        return objectId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReturnHistory that = (ReturnHistory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
