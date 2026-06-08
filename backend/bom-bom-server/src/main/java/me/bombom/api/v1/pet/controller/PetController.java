package me.bombom.api.v1.pet.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.auth.CurrentMemberProvider;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.pet.service.PetService;
import me.bombom.openapi.api.PetApi;
import me.bombom.openapi.common.PetResponseMapper;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PetController implements PetApi {

    private final CurrentMemberProvider currentMemberProvider;
    private final PetService petService;

    @Override
    public me.bombom.openapi.model.PetResponse getPet() {
        Member member = currentMemberProvider.getCurrentMember();
        return PetResponseMapper.toApi(petService.getPet(member));
    }

    @Override
    public void attend() {
        Member member = currentMemberProvider.getCurrentMember();
        petService.attend(member);
    }
}
