package me.bombom.api.v1.common;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties("bom-bom.rate-limit.read-count")
public class ReadRateLimitProperties {

    @Min(1)
    private int bucketCapacity; //TODO: 검증 실패하면 그냥 기본값 사용하도록?

    @Min(1)
    private int refillSeconds;
}
