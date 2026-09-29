package me.bombom.api.v1.inquiry.event;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.DiscordWebhookNotifier;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberDiscordAccountRepository;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryMessageSentDiscordListener {

    private final DiscordWebhookNotifier discordWebhookNotifier;
    private final MemberRepository memberRepository;
    private final MemberDiscordAccountRepository memberDiscordAccountRepository;

    @Async("inquiryDiscordExecutor")
    @TransactionalEventListener
    public void on(InquiryMessageSentEvent event) {
        try {
            String assigneeText = resolveAssignee(event.assigneeId()).orElse(null);
            discordWebhookNotifier.sendInquiryNewMessageNotification(event.content(), assigneeText);
        } catch (Exception e) {
            log.debug("⚠️ Discord 알림 전송 실패 (무시): {}", e.getMessage());
        }
    }

    // 담당자가 지정되지 않은 경우 "미지정" 반환
    // 담당자가 정상적으로 조회되면 디스코드 멘션 텍스트 반환
    // 담당자의 디스코드 ID 조회에 실패하면 닉네임 반환
    // 담당자 정보 조회에 실패하면(assigneeId에 대한 멤버를 찾을 수 없거나 조회 중 예외 발생) Optional.empty() 반환
    private Optional<String> resolveAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return Optional.of("미지정");
        }
        try {
            return memberDiscordAccountRepository.findByMemberId(assigneeId)
                    .map(account -> "<@" + account.getDiscordId() + ">")
                    .or(() -> findNickname(assigneeId))
                    .or(() -> {
                        log.warn("담당자 정보를 찾을 수 없음 (assigneeId: {})", assigneeId);
                        return Optional.empty();
                    });
        } catch (Exception e) {
            log.warn("담당자 조회 중 예외 발생 (assigneeId: {})", assigneeId, e);
            return Optional.empty();
        }
    }

    private Optional<String> findNickname(Long assigneeId) {
        return memberRepository.findById(assigneeId)
                .map(Member::getNickname);
    }
}
