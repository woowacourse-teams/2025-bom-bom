package me.bombom.api.v1.newsletterrequest.dto.response;

public enum NewsletterRequestCheckResult {

    AVAILABLE,  // 신청 가능
    REQUESTED,  // 같은 링크의 신청이 진행 중
    REGISTERED  // 이미 봄봄에 등록된 뉴스레터
}
