package me.bombom.api.v1.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("bom-bom.rate-limit.read-count")
public class ReadRateLimitProperties {

    private int bucketCapacity = 3;

    private int refillSeconds = 50;
}
