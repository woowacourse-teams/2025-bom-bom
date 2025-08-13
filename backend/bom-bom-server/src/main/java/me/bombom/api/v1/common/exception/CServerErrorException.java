package me.bombom.api.v1.common.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CServerErrorException extends RuntimeException{

    private final ErrorDetail errorDetail;
    private final Map<String, Object> context = new HashMap<>();

    public CServerErrorException(ErrorDetail errorDetail) {
        super(errorDetail.getMessage());
        this.errorDetail = errorDetail;
    }

    public CServerErrorException addContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }

    public HttpStatus getHttpStatus(){
        return errorDetail.getStatus();
    }
}
