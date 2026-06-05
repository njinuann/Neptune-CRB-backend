package com.neptune.crb.processing.model.creditInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CRBCreditInfoResponse<T> {

    private String responseCode;
    private String message;
    private T data;
}