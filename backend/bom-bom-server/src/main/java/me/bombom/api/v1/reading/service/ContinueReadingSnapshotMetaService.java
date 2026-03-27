package me.bombom.api.v1.reading.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.reading.domain.ContinueReadingSnapshotMeta;
import me.bombom.api.v1.reading.repository.ContinueReadingSnapshotMetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContinueReadingSnapshotMetaService {

    private static final Long SINGLE_ID = 1L;

    private final ContinueReadingSnapshotMetaRepository continueReadingRankingSnapshotMetaRepository;

    public LocalDateTime getSnapshotAt() {
        return continueReadingRankingSnapshotMetaRepository.findById(SINGLE_ID)
                .map(ContinueReadingSnapshotMeta::getSnapshotAt)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.MEMBER_ID, SINGLE_ID)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "ContinueReadingSnapshotMeta"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSnapshotAt() {
        int updated = continueReadingRankingSnapshotMetaRepository.updateSnapshotAt(SINGLE_ID);
        if (updated == 0) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "ContinueReadingSnapshotMeta")
                    .addContext(ErrorContextKeys.MEMBER_ID, SINGLE_ID);
        }
    }
}
