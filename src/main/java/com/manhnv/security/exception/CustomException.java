package com.manhnv.security.exception;

import com.manhnv.security.component.IErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private IErrorCode errorCode;

    public CustomException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException(String message) {
        super(message);
    }

}
