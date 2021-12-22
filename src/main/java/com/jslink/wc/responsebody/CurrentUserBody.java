package com.jslink.wc.responsebody;

import lombok.Data;

@Data
public class CurrentUserBody {
    private boolean success;
    private AccountBody data;
}
