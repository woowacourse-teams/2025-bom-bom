package me.bombom.api.v1.newsletterrequest.dto.response;

public record CheckNewsletterRequestResponse(
        NewsletterRequestCheckResult result,
        Long newsletterRequestId,
        Long newsletterId
) {

    public static CheckNewsletterRequestResponse available() {
        return new CheckNewsletterRequestResponse(NewsletterRequestCheckResult.AVAILABLE, null, null);
    }

    public static CheckNewsletterRequestResponse requested(Long newsletterRequestId) {
        return new CheckNewsletterRequestResponse(NewsletterRequestCheckResult.REQUESTED, newsletterRequestId, null);
    }

    public static CheckNewsletterRequestResponse registered(Long newsletterId) {
        return new CheckNewsletterRequestResponse(NewsletterRequestCheckResult.REGISTERED, null, newsletterId);
    }
}
