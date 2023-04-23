package com.jslink.wc.responsebody;

import lombok.Data;

import java.util.List;

/**
 * 依据Ant design 需要的数据格式定义
 */
@Data
public class BaseBody<T> {
    private List<T> data;
    private Integer total;
    private boolean success = true;
    private int pageSize;
    private int current;
}
