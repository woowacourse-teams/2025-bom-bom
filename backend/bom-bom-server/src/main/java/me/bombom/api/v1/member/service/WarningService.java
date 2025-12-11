package me.bombom.api.v1.member.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.member.domain.WarningSetting;
import me.bombom.api.v1.member.dto.request.UpdateWarningSettingRequest;
import me.bombom.api.v1.member.dto.response.WarningSettingResponse;
import me.bombom.api.v1.member.repository.WarningSettingRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarningService {

    private final WarningSettingRepository warningSettingRepository;

    public WarningSettingResponse getWarningSetting(Member member) {
        WarningSetting setting = findWarningSettingByMemberId(member);
        return WarningSettingResponse.from(setting);
    }

    @Transactional
    public void updateWarningSetting(Member member, UpdateWarningSettingRequest request) {
        WarningSetting setting = findWarningSettingByMemberId(member);
        setting.updateVisibility(request.isVisible());
    }

    private WarningSetting findWarningSettingByMemberId(Member member) {
        WarningSetting setting = warningSettingRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "warning_setting")
                        .addContext(ErrorContextKeys.MEMBER_ID, member.getId()));
        return setting;
    }
}
