package news.bombom.captcha.client;

public class RecaptchaApiException extends RuntimeException {

    public RecaptchaApiException(String message) {
        super(message);
    }

    public RecaptchaApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
