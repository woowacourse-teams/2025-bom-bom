package me.bombom.api.v1.common.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CIllegalArgumentException extends RuntimeException {

    private final ErrorDetail errorDetail;
    private final Map<String, Object> context = new HashMap<>();

    public CIllegalArgumentException(ErrorDetail errorDetail) {
        super(errorDetail.getMessage());
        this.errorDetail = errorDetail;
    }

    public CIllegalArgumentException addContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }

    /**
     * ErrorContextKeys enum을 사용하여 타입 안전한 컨텍스트 추가
     * 
     * 사용 가이드라인:
     * - 여러 서비스에서 공통으로 사용되는 키는 ErrorContextKeys enum 사용
     * - 특정 상황에서만 사용되는 키는 String을 직접 사용
     * - 둘다 혼용 가능: .addContext(ErrorContextKeys.MEMBER_ID, id).addContext("customKey", value)
     * 
     * @param key ErrorContextKeys enum 값
     * @param value 컨텍스트 값
     * @return 체이닝을 위한 자기 자신 반환
     */
    public CIllegalArgumentException addContext(ErrorContextKeys key, Object value) {
        this.context.put(key.getKey(), value);
        return this;
    }

    public HttpStatus getHttpStatus(){
      return errorDetail.getStatus();
    }
}
