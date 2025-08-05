package me.bombom.api.v1.pet.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.pet.dto.PetResponse;
import me.bombom.api.v1.pet.service.PetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me/pet")
public class PetController implements PetControllerApi{

    private final PetService petService;

    @Override
    @GetMapping
    public PetResponse getPet(@LoginMember Member member){
        return petService.getPet(member);
    }

    @Override
    @PostMapping("/attendance")
    public void addAttendanceScore(@LoginMember Member member){
        petService.addAttendanceScore(member);
    }
}
