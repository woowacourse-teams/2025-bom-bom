package me.bombom.api.v1.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CIllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(CIllegalArgumentException e){
        return ResponseEntity.status(e.getHttpStatus())
                .body(ErrorResponse.from(e.getErrorDetail()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        return ResponseEntity.status(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION.getStatus())
                .body(ErrorResponse.from(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION));
    }
}
