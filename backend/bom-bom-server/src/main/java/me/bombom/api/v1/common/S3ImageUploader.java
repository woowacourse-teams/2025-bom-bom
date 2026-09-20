package me.bombom.api.v1.common;

import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;

@Component
@RequiredArgsConstructor
public class S3ImageUploader {

    private final S3Template s3Template;

    public String upload(String bucketName, String key, InputStream inputStream) {
        try {
            S3Resource uploaded = s3Template.upload(bucketName, key, inputStream);
            return uploaded.getURL().toString();
        } catch (S3Exception e) {
            throw new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext("operation", "s3Upload")
                    .addContext("bucketName", bucketName)
                    .addContext("key", key)
                    .addContext("message", e.getMessage());
        } catch (SdkException | IOException e) {
            throw new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext("operation", "s3GetImageUrl")
                    .addContext("bucketName", bucketName)
                    .addContext("key", key)
                    .addContext("message", e.getMessage());
        }
    }
}
