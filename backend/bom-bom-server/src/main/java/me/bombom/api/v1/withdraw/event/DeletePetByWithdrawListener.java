package me.bombom.api.v1.withdraw.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.pet.service.PetService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeletePetByWithdrawListener {

    private final PetService petService;

    @TransactionalEventListener
    public void on(WithdrawEvent event) {
        log.info("회원 탈퇴 따른 키우기 삭제 시작 - memberId={}", event.memberId());
        try {
            petService.deleteByMemberId(event.memberId());
        } catch (Exception e) {
            log.error("회원 탈퇴 따른 키우기 삭제 실패 - memberId={}", event.memberId(), e);
        }
    }
}
