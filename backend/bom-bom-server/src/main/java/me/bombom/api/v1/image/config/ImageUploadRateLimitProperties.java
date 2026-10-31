package me.bombom.api.v1.image.config;

import me.bombom.api.v1.image.domain.ImageUploadOwnerType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("bom-bom.rate-limit.image-upload")
public record ImageUploadRateLimitProperties(

        @DefaultValue Policy member,
        @DefaultValue Policy guest
) {

    public Policy getPolicy(ImageUploadOwnerType ownerType) {
        return switch (ownerType) {
            case MEMBER -> member;
            case GUEST -> guest;
        };
    }

    public record Policy(

            @DefaultValue("30") int bucketCapacity,
            @DefaultValue("1800") int refillSeconds
    ) {
    }
}
