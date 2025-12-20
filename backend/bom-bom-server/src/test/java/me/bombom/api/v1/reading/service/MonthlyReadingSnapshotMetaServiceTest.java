package me.bombom.api.v1.reading.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class MonthlyReadingSnapshotMetaServiceTest {

    @Autowired
    MonthlyReadingSnapshotMetaService monthlyReadingSnapshotMetaService;

    @Test
    void 스냅샷_시간이_업데이트되면_현재시각_근처로_갱신된다() {
        // given
        LocalDateTime beforeSnapshotAt = monthlyReadingSnapshotMetaService.getSnapshotAt();

        // when
        monthlyReadingSnapshotMetaService.updateSnapshotAt();
        LocalDateTime afterSnapshotAt = monthlyReadingSnapshotMetaService.getSnapshotAt();

        // then
        assertThat(afterSnapshotAt).isAfter(beforeSnapshotAt);
    }

    @Test
    void 스냅샷_시간을_가져올_때_예외가_발생하지_않는다() {
        // given
        // when
        // then
        assertThatCode(() -> monthlyReadingSnapshotMetaService.getSnapshotAt());
    }
}
