package me.bombom.api.v1.member.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.response.MemberInfoResponse;
import me.bombom.api.v1.member.service.MemberService;
import me.bombom.api.v1.member.dto.request.MemberInfoUpdateRequest;
import me.bombom.api.v1.member.dto.response.MemberProfileResponse;
import me.bombom.api.v1.subscribe.dto.SubscribedNewsletterResponse;
import me.bombom.api.v1.subscribe.service.SubscribeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController implements MemberControllerApi {

  private final MemberService memberService;
  private final SubscribeService subscribeService;

  @Override
  @GetMapping("/me")
  public MemberInfoResponse getMemberInfo(@LoginMember Member member) {
    return memberService.getInfo(member.getId());
  }

  @Override
  @PatchMapping("/me")
  public MemberInfoResponse updateMemberInfo(@LoginMember Member member, @Valid @RequestBody MemberInfoUpdateRequest request) {
    return memberService.updateInfo(member.getId(), request);
  }

  @Override
  @GetMapping("/me/profile")
  public MemberProfileResponse getMemberProfile(@LoginMember Member member) {
    return memberService.getProfile(member.getId());
  }

  @Override
  @GetMapping("/me/newsletters")
  public List<SubscribedNewsletterResponse> getSubscribedNewsletters(@LoginMember Member member) {
    return subscribeService.getSubscribedNewsletters(member);
  }
}
