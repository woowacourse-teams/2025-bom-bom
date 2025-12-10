package me.bombom.api.v1.article.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.domain.WarningSetting;
import me.bombom.api.v1.article.dto.response.WarningSettingResponse;
import me.bombom.api.v1.article.repository.WarningSettingRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarningService {

    private final WarningSettingRepository warningSettingRepository;

    public WarningSettingResponse getCapacityWarningStatus(Member member) {
        WarningSetting setting = warningSettingRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "warning_setting")
                        .addContext(ErrorContextKeys.MEMBER_ID, member.getId()));
        return WarningSettingResponse.from(setting);
    }
}
