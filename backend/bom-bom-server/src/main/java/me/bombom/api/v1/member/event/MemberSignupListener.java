package me.bombom.api.v1.member.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.pet.service.PetService;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberSignupListener {

    private final ReadingService readingService;
    private final PetService petService;

    @TransactionalEventListener
    public void on(MemberSignupEvent event) {
        try {
            readingService.initializeReadingInformation(event.memberId());
            petService.createPet(event.memberId());
        } catch (Exception e) {
            log.error("읽기 정보 초기화에 실패했습니다.");
        }
    }
}
