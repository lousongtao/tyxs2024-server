package com.jslink.wc.responsebody;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> data;
    private int page;
    private boolean success = true;
    private long total;

    public PageResult(List<T> data, int page, long total) {
        this.data = data;
        this.page = page;
        this.total = total;
    }

    public PageResult(List<T> data, int page, boolean success, long total) {
        this.data = data;
        this.page = page;
        this.success = success;
        this.total = total;
    }
}
