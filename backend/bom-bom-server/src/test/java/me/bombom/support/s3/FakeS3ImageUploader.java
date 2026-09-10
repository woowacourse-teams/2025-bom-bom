package me.bombom.support.s3;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import me.bombom.api.v1.common.S3ImageUploader;
import me.bombom.support.testdouble.ResettableTestDouble;

/**
 * 이미지 업로드 통합 테스트에서 실제 S3에 접근하지 않고 업로드 요청 내역을 기록하고
 * 고정된 형태의 URL을 반환한다.
 */
public class FakeS3ImageUploader extends S3ImageUploader implements ResettableTestDouble {

    private final List<UploadRequest> uploadRequests = new ArrayList<>();

    public FakeS3ImageUploader() {
        super(null);
    }

    @Override
    public String upload(String bucketName, String key, InputStream inputStream) {
        uploadRequests.add(new UploadRequest(bucketName, key));
        return "https://" + bucketName + ".s3.amazonaws.com/" + key;
    }

    public List<UploadRequest> getUploadRequests() {
        return List.copyOf(uploadRequests);
    }

    @Override
    public void reset() {
        uploadRequests.clear();
    }

    public record UploadRequest(String bucketName, String key) {
    }
}
