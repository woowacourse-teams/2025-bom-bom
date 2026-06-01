package me.bombom.api.v1.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("bom-bom.rate-limit.read-count")
public record ReadRateLimitProperties(

        @DefaultValue("3") int bucketCapacity,
        @DefaultValue("50") int refillSeconds
) {
}
