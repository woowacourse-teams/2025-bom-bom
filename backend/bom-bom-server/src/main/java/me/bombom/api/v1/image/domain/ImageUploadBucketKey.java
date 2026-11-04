package me.bombom.api.v1.image.domain;

import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;

/**
 * 이미지 업로드 rate limit 버킷을 식별하는 키. '회원타입:식별값' 형식이며, 이 클래스를 통해서만 생성해 형식이 한 곳에서만 정해지도록 한다.
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

    public static ImageUploadBucketKey fromMemberId(Long memberId) {
        if (memberId == null) {
            throw new CIllegalArgumentException(ErrorDetail.MISSING_REQUESTER_IDENTIFIER);
        }
        return create(ImageUploadOwnerType.MEMBER, String.valueOf(memberId));
    }

    public static ImageUploadBucketKey fromGuestId(String guestId) {
        if (guestId == null) {
            throw new CIllegalArgumentException(ErrorDetail.MISSING_REQUESTER_IDENTIFIER);
        }
        return create(ImageUploadOwnerType.GUEST, guestId);
    }

    public static ImageUploadBucketKey ofRequester(Long memberId, String guestId) {
        if (memberId != null) {
            return fromMemberId(memberId);
        }
        return fromGuestId(guestId);
    }

    public static String keyPrefixOf(ImageUploadOwnerType ownerType) {
        return ownerType.getKeyPrefix() + DELIMITER;
    }

    private static ImageUploadBucketKey create(ImageUploadOwnerType ownerType, String identifier) {
        return new ImageUploadBucketKey(ownerType, keyPrefixOf(ownerType) + identifier);
    }
}
