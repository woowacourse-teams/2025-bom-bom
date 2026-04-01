package me.bombom.api.v1.reading.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.reading.domain.ReadingSnapshotMeta;
import me.bombom.api.v1.reading.domain.ReadingSnapshotType;
import me.bombom.api.v1.reading.repository.ReadingSnapshotMetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingSnapshotMetaService {

    private final ReadingSnapshotMetaRepository readingSnapshotMetaRepository;

    public LocalDateTime getSnapshotAt(ReadingSnapshotType snapshotType) {
        return readingSnapshotMetaRepository.findById(snapshotType)
                .map(ReadingSnapshotMeta::getSnapshotAt)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSnapshotAt(ReadingSnapshotType snapshotType) {
        int updated = readingSnapshotMetaRepository.updateSnapshotAt(snapshotType.name());
        if (updated == 0) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND);
        }
    }
}
