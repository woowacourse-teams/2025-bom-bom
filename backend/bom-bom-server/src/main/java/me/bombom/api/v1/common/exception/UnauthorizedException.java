package me.bombom.api.v1.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class UnauthorizedException extends RuntimeException {

    private final ErrorDetail errorDetail;

    public HttpStatus getHttpStatus(){
        return errorDetail.getStatus();
    }
}
