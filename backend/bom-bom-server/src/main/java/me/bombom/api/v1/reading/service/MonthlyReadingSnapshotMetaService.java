package me.bombom.api.v1.reading.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.reading.domain.MonthlyReadingSnapshotMeta;
import me.bombom.api.v1.reading.repository.MonthlyReadingSnapshotMetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MonthlyReadingSnapshotMetaService {

    private static final Long SINGLE_ID = 1L;

    private final MonthlyReadingSnapshotMetaRepository monthlyReadingSnapshotMetaRepository;

    public LocalDateTime getSnapshotAt() {
        return monthlyReadingSnapshotMetaRepository.findById(SINGLE_ID)
                .map(MonthlyReadingSnapshotMeta::getSnapshotAt)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                .addContext(ErrorContextKeys.MEMBER_ID, SINGLE_ID)
                .addContext(ErrorContextKeys.ENTITY_TYPE, "MonthlyReadingSnapshotMeta"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSnapshotAt() {
        int updated = monthlyReadingSnapshotMetaRepository.updateSnapshotAt(SINGLE_ID);
        if (updated == 0) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "MonthlyReadingSnapshotMeta")
                    .addContext(ErrorContextKeys.MEMBER_ID, SINGLE_ID);
        }
    }
}
