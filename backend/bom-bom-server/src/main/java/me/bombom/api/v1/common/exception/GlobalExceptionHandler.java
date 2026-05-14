package me.bombom.api.v1.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CIllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(CIllegalArgumentException e) {
        if (!e.getContext().isEmpty()) {
            log.info("IllegalArgumentException: {} - Context: {}", e.getMessage(), e.getContext(), e);
        } else {
            log.info("IllegalArgumentException: ", e);
        }
        return ResponseEntity.status(e.getHttpStatus())
                .body(ErrorResponse.from(e.getErrorDetail()));
    }

    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<ErrorResponse> handleConversionFailedException(ConversionFailedException e) {
        Throwable cause = e.getCause();
        if (cause instanceof CIllegalArgumentException illegalArg) {
            return handleIllegalArgumentException(illegalArg);
        }
        log.info("Conversion failed: ", e);
        return ResponseEntity.status(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION.getStatus())
                .body(ErrorResponse.from(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e
    ) {
        Throwable cause = e.getCause();
        if (cause instanceof ConversionFailedException conversionFailed) {
            return handleConversionFailedException(conversionFailed);
        }
        if (cause instanceof CIllegalArgumentException illegalArg) {
            return handleIllegalArgumentException(illegalArg);
        }
        log.info("Method argument type mismatch: ", e);
        return ResponseEntity.status(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION.getStatus())
                .body(ErrorResponse.from(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException e) {
        if (!e.getContext().isEmpty()) {
            log.warn("UnauthorizedException: {} - Context: {}", e.getMessage(), e.getContext(), e);
        } else {
            log.warn("UnauthorizedException: ", e);
        }
        return ResponseEntity.status(e.getHttpStatus())
                .body(ErrorResponse.from(e.getErrorDetail()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        log.info("Validation failed: ", e);
        return ResponseEntity.status(ErrorDetail.INVALID_REQUEST_BODY_VALIDATION.getStatus())
                .body(ErrorResponse.from(ErrorDetail.INVALID_REQUEST_BODY_VALIDATION));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e){
        log.info("Constraint violation: ", e);
        return ResponseEntity.status(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION.getStatus())
                .body(ErrorResponse.from(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION));
    }

    @ExceptionHandler(CServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleCServerErrorException(CServerErrorException e){
        if (!e.getContext().isEmpty()) {
            log.error("CServerErrorException: {} - Context: {}", e.getMessage(), e.getContext(), e);
        } else {
            log.error("CServerErrorException: ", e);
        }
        return ResponseEntity.status(e.getHttpStatus())
                .body(ErrorResponse.from(e.getErrorDetail()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("No resource found: ", e);
        return ResponseEntity.status(ErrorDetail.ENTITY_NOT_FOUND.getStatus())
                .body(ErrorResponse.from(ErrorDetail.ENTITY_NOT_FOUND));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        log.info("Request body parse error: ", e);
        return ResponseEntity.status(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION.getStatus())
                .body(ErrorResponse.from(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception: ", e);
        return ResponseEntity.status(ErrorDetail.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.from(ErrorDetail.INTERNAL_SERVER_ERROR));
    }
}
