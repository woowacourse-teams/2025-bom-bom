package me.bombom.api.v1.newsletterrequest.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequest;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestDraft;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestStatus;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterUrl;
import me.bombom.api.v1.newsletterrequest.dto.NewsletterRequestRow;
import me.bombom.api.v1.newsletterrequest.dto.request.CreateNewsletterRequestRequest;
import me.bombom.api.v1.newsletterrequest.dto.response.CheckNewsletterRequestResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.CreateNewsletterRequestResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestCheckResult;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestLikeResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestSuggestionResponse;
import me.bombom.api.v1.newsletterrequest.repository.NewsletterRequestDraftRepository;
import me.bombom.api.v1.newsletterrequest.repository.NewsletterRequestLikeRepository;
import me.bombom.api.v1.newsletterrequest.repository.NewsletterRequestRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterRequestService {

    private static final int BOARD_SIZE = 50;
    private static final int APPROVED_VISIBLE_DAYS = 30;
    private static final int SUGGESTION_SIZE = 3;
    private static final int SUGGESTION_KEYWORD_MIN_LENGTH = 2;

    private final NewsletterRequestRepository newsletterRequestRepository;
    private final NewsletterRequestLikeRepository newsletterRequestLikeRepository;
    private final NewsletterRequestDraftRepository newsletterRequestDraftRepository;
    private final Clock clock;

    public CheckNewsletterRequestResponse checkNewsletterRequest(String rawUrl) {
        return check(NewsletterUrl.from(rawUrl));
    }

    public List<NewsletterRequestResponse> getNewsletterRequests(Member member) {
        List<NewsletterRequestRow> rows = newsletterRequestRepository.findBoardRows(
                NewsletterRequestStatus.IN_PROGRESS,
                NewsletterRequestStatus.APPROVED,
                LocalDateTime.now(clock).minusDays(APPROVED_VISIBLE_DAYS),
                PageRequest.of(0, BOARD_SIZE)
        );
        return toResponses(rows, member);
    }

    public List<NewsletterRequestResponse> getMyNewsletterRequests(Member member) {
        return toResponses(newsletterRequestRepository.findRowsRequestedOrLikedBy(member.getId()), member);
    }

    public NewsletterRequestSuggestionResponse getNewsletterRequestSuggestions(Member member, String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.length() < SUGGESTION_KEYWORD_MIN_LENGTH) {
            return NewsletterRequestSuggestionResponse.empty();
        }
        PageRequest pageRequest = PageRequest.of(0, SUGGESTION_SIZE);
        List<NewsletterRequestRow> rows = newsletterRequestRepository.findRowsByKeyword(
                NewsletterRequestStatus.IN_PROGRESS,
                normalizedKeyword,
                pageRequest
        );
        return new NewsletterRequestSuggestionResponse(
                toResponses(rows, member),
                newsletterRequestRepository.findNewsletterSuggestions(normalizedKeyword, pageRequest)
        );
    }

    @Transactional
    public CreateNewsletterRequestResponse createNewsletterRequest(Member member, CreateNewsletterRequestRequest request) {
        NewsletterUrl newsletterUrl = NewsletterUrl.from(request.url());
        validateAvailable(newsletterUrl);

        NewsletterRequest newsletterRequest = newsletterRequestRepository.findByNormalizedUrl(newsletterUrl.getNormalized())
                .map(rejected -> reopen(rejected, newsletterUrl, member, request))
                .orElseGet(() -> register(newsletterUrl, member, request));
        return new CreateNewsletterRequestResponse(newsletterRequest.getId());
    }

    @Transactional
    public NewsletterRequestLikeResponse addNewsletterRequestLike(Member member, Long newsletterRequestId) {
        NewsletterRequest newsletterRequest = findNewsletterRequest(newsletterRequestId);
        validateLikeable(newsletterRequest, member.getId());

        int insertCount = newsletterRequestLikeRepository.bulkInsertIgnoreByMemberIdAndNewsletterRequestId(
                member.getId(),
                newsletterRequestId
        );
        if (insertCount == 1) {
            newsletterRequestRepository.bulkIncrementLikeCountNotBelowZero(newsletterRequestId, 1);
        }
        return NewsletterRequestLikeResponse.from(findNewsletterRequest(newsletterRequestId));
    }

    @Transactional
    public NewsletterRequestLikeResponse deleteNewsletterRequestLike(Member member, Long newsletterRequestId) {
        findNewsletterRequest(newsletterRequestId);

        int deleteCount = newsletterRequestLikeRepository.deleteByMemberIdAndNewsletterRequestId(
                member.getId(),
                newsletterRequestId
        );
        if (deleteCount == 1) {
            newsletterRequestRepository.bulkIncrementLikeCountNotBelowZero(newsletterRequestId, -1);
        }
        return NewsletterRequestLikeResponse.from(findNewsletterRequest(newsletterRequestId));
    }

    private CheckNewsletterRequestResponse check(NewsletterUrl newsletterUrl) {
        Optional<Long> registeredNewsletterId = findRegisteredNewsletterId(newsletterUrl.getNormalized());
        if (registeredNewsletterId.isPresent()) {
            return CheckNewsletterRequestResponse.registered(registeredNewsletterId.get());
        }
        return newsletterRequestRepository.findByNormalizedUrl(newsletterUrl.getNormalized())
                .filter(NewsletterRequest::isInProgress)
                .map(inProgress -> CheckNewsletterRequestResponse.requested(inProgress.getId()))
                .orElseGet(CheckNewsletterRequestResponse::available);
    }

    private Optional<Long> findRegisteredNewsletterId(String normalizedUrl) {
        return newsletterRequestRepository.findRegisteredNewsletterUrls()
                .stream()
                .filter(registered -> normalizedUrl.equals(NewsletterUrl.normalize(registered.mainPageUrl()))
                        || normalizedUrl.equals(NewsletterUrl.normalize(registered.subscribeUrl())))
                .map(registered -> registered.newsletterId())
                .findFirst();
    }

    private void validateAvailable(NewsletterUrl newsletterUrl) {
        CheckNewsletterRequestResponse checked = check(newsletterUrl);
        if (checked.result() != NewsletterRequestCheckResult.AVAILABLE) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletterRequest")
                    .addContext("result", checked.result())
                    .addContext("normalizedUrl", newsletterUrl.getNormalized());
        }
    }

    private void validateLikeable(NewsletterRequest newsletterRequest, Long memberId) {
        if (!newsletterRequest.isInProgress()) {
            throw new CIllegalArgumentException(ErrorDetail.PRECONDITION_FAILED)
                    .addContext("newsletterRequestId", newsletterRequest.getId())
                    .addContext("status", newsletterRequest.getStatus());
        }
        if (newsletterRequest.isRequestedBy(memberId)) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext("newsletterRequestId", newsletterRequest.getId())
                    .addContext(ErrorContextKeys.DETAIL, "requester cannot like own request");
        }
    }

    private NewsletterRequest register(
            NewsletterUrl newsletterUrl,
            Member member,
            CreateNewsletterRequestRequest request
    ) {
        NewsletterRequest newsletterRequest = saveNewRequest(NewsletterRequest.builder()
                .requestedName(request.name().strip())
                .requestedUrl(newsletterUrl.getOriginal())
                .normalizedUrl(newsletterUrl.getNormalized())
                .requesterMemberId(member.getId())
                .reason(blankToNull(request.reason()))
                .isNotificationEnabled(request.isNotificationEnabled())
                .build());
        newsletterRequestDraftRepository.save(NewsletterRequestDraft.builder()
                .newsletterRequestId(newsletterRequest.getId())
                .build());
        return newsletterRequest;
    }

    private NewsletterRequest saveNewRequest(NewsletterRequest newsletterRequest) {
        try {
            return newsletterRequestRepository.saveAndFlush(newsletterRequest);
        } catch (DataIntegrityViolationException exception) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletterRequest")
                    .addContext("normalizedUrl", newsletterRequest.getNormalizedUrl());
        }
    }

    private NewsletterRequest reopen(
            NewsletterRequest rejected,
            NewsletterUrl newsletterUrl,
            Member member,
            CreateNewsletterRequestRequest request
    ) {
        rejected.reopen(
                request.name().strip(),
                newsletterUrl.getOriginal(),
                member.getId(),
                blankToNull(request.reason()),
                request.isNotificationEnabled()
        );
        newsletterRequestLikeRepository.deleteByNewsletterRequestId(rejected.getId());
        newsletterRequestDraftRepository.findByNewsletterRequestId(rejected.getId())
                .ifPresentOrElse(
                        NewsletterRequestDraft::resetToPending,
                        () -> newsletterRequestDraftRepository.save(NewsletterRequestDraft.builder()
                                .newsletterRequestId(rejected.getId())
                                .build())
                );
        return rejected;
    }

    private NewsletterRequest findNewsletterRequest(Long newsletterRequestId) {
        return newsletterRequestRepository.findById(newsletterRequestId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletterRequest")
                        .addContext("newsletterRequestId", newsletterRequestId));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }

    private List<NewsletterRequestResponse> toResponses(List<NewsletterRequestRow> rows, Member member) {
        Long memberId = member == null ? null : member.getId();
        Set<Long> likedIds = findLikedIds(rows, memberId);
        return rows.stream()
                .map(row -> NewsletterRequestResponse.of(row, likedIds.contains(row.id()), memberId))
                .toList();
    }

    private Set<Long> findLikedIds(List<NewsletterRequestRow> rows, Long memberId) {
        if (memberId == null || rows.isEmpty()) {
            return Set.of();
        }
        List<Long> newsletterRequestIds = rows.stream()
                .map(NewsletterRequestRow::id)
                .toList();
        return new HashSet<>(newsletterRequestLikeRepository.findLikedNewsletterRequestIds(memberId, newsletterRequestIds));
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.replaceAll("[\\s%_\\\\]", "").toLowerCase(Locale.ROOT);
    }
}
