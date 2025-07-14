package me.bombom.api.v1.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorDetail {

    /*
    * M : 모두 사용
    */
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "M001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "M002", "허용되지 않는 접근입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "M003", "존재하지 않는 데이터입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "M004", "서버에서 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "M005", "잘못된 타입입니다."),
    INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "M006", "Json 형식과 맞지 않습니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "M007", "권한이 없습니다."),
    INVALID_REQUEST_PARAMETER_VALIDATION(HttpStatus.BAD_REQUEST, "M008", "요청 파라미터 유효성이 맞지 않습니다."),

    /*
    * I : 인증
    */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "I001", "로그인이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "I002", "유효하지 않은 인증 정보입니다. 다시 로그인 해주세요."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "I003", "유효하지 않은 인증 정보입니다. 다시 로그인 해주세요."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
