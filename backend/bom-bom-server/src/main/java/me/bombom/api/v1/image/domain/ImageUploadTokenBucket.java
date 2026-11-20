package me.bombom.api.v1.image.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageUploadTokenBucket {

    @Id
    @Column(length = ImageUploadBucketKey.MAX_LENGTH)
    private String bucketKey;

    @Column(nullable = false)
    private double tokens;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private ImageUploadTokenBucket(String bucketKey, double tokens, LocalDateTime updatedAt) {
        this.bucketKey = bucketKey;
        this.tokens = tokens;
        this.updatedAt = updatedAt;
    }
}
