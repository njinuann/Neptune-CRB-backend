package com.neptune.crb.processing.model.creditInfo;

import lombok.Data;

@Data
public class StrategyResponse {

    private String token;
    private String status;
    private Object data;
}