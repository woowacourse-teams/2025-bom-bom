package news.bombom.notification.domain;

public record SendResult(SendStatus status, String errorMessage, String errorCode) {

    public static SendResult success() {
        return new SendResult(SendStatus.SUCCESS, null, null);
    }

    public static SendResult failed(String errorMessage) {
        return failed(errorMessage, null);
    }

    public static SendResult failed(String errorMessage, String errorCode) {
        return new SendResult(SendStatus.FAILED, errorMessage, errorCode);
    }

    public static SendResult skipped() {
        return new SendResult(SendStatus.SKIPPED, null, null);
    }

    public enum SendStatus {
        SUCCESS, FAILED, SKIPPED
    }
}
