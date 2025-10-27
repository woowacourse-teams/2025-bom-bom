package news.bombom.notification.domain;

public record SendResult(SendStatus status, String errorMessage) {

    public static SendResult success() {
        return new SendResult(SendStatus.SUCCESS, null);
    }

    public static SendResult failed(String errorMessage) {
        return new SendResult(SendStatus.FAILED, errorMessage);
    }

    public static SendResult skipped() {
        return new SendResult(SendStatus.SKIPPED, null);
    }

    public enum SendStatus {
        SUCCESS, FAILED, SKIPPED
    }
}
