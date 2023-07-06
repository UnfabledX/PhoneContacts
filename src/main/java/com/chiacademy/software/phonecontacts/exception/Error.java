package com.chiacademy.software.phonecontacts.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error {

    private String message;
    private String field;
    private String wrongValue;
}
