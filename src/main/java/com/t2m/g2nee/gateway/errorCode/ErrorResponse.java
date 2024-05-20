package com.t2m.g2nee.gateway.errorCode;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

    private int code;
    private String message;

}