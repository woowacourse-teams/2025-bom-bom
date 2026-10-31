package me.bombom.api.v1.image.domain;

import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;

/**
 * 이미지 업로드 rate limit 버킷을 식별하는 키.
 * '회원타입:식별값' 형식이며, 이 클래스를 통해서만 생성해 형식이 한 곳에서만 정해지도록 한다.
 */
public record ImageUploadBucketKey(ImageUploadOwnerType ownerType, String value) {

    public static final int MAX_LENGTH = 48;

    private static final String DELIMITER = ":";

    public ImageUploadBucketKey {
        if (value.length() > MAX_LENGTH) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext("reason", "image_upload_bucket_key_too_long")
                    .addContext("length", value.length());
        }
    }

    public static ImageUploadBucketKey of(Long memberId, String guestId) {
        if (memberId != null) {
            return create(ImageUploadOwnerType.MEMBER, String.valueOf(memberId));
        }
        if (guestId != null) {
            return create(ImageUploadOwnerType.GUEST, guestId);
        }
        throw new CIllegalArgumentException(ErrorDetail.MISSING_REQUESTER_IDENTIFIER);
    }

    private static ImageUploadBucketKey create(ImageUploadOwnerType ownerType, String identifier) {
        return new ImageUploadBucketKey(ownerType, ownerType.getKeyPrefix() + DELIMITER + identifier);
    }
}
