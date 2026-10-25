package me.bombom.api.v1.subscribe.client;

public class WebRiskUnavailableException extends RuntimeException {

    public WebRiskUnavailableException() {
        super("Web Risk 검사를 완료하지 못했습니다.");
    }
}
