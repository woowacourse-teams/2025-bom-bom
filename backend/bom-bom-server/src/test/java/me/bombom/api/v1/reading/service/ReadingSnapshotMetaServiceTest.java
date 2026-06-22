package me.bombom.api.v1.reading.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import me.bombom.api.v1.reading.domain.ReadingSnapshotType;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ReadingSnapshotMetaServiceTest {

    @Autowired
    ReadingSnapshotMetaService readingSnapshotMetaService;

    @Test
    void 월간_스냅샷_시간이_업데이트되면_현재시각_근처로_갱신된다() {
        // given
        LocalDateTime beforeSnapshotAt = readingSnapshotMetaService.getSnapshotAt(ReadingSnapshotType.MONTHLY);

        // when
        readingSnapshotMetaService.updateSnapshotAt(ReadingSnapshotType.MONTHLY);
        LocalDateTime afterSnapshotAt = readingSnapshotMetaService.getSnapshotAt(ReadingSnapshotType.MONTHLY);

        // then
        assertThat(afterSnapshotAt).isAfter(beforeSnapshotAt);
    }

    @Test
    void 연속_읽기_스냅샷_시간을_가져올_때_예외가_발생하지_않는다() {
        // given
        // when
        // then
        assertThatCode(() -> readingSnapshotMetaService.getSnapshotAt(ReadingSnapshotType.CONTINUE));
    }
}
