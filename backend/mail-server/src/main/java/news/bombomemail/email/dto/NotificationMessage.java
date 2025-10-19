package news.bombomemail.email.dto;

public record NotificationMessage(
        String targetToken,
        String title,
        String body
) {
}
