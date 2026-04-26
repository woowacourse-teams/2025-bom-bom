package news.bombomemail.common;

import static org.assertj.core.api.Assertions.assertThat;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(SchedulingConfig.class)
class SchedulingConfigTest {

    @Autowired
    private LockProvider lockProvider;

    @Test
    void scheduler_lock_provider_bean을_로딩한다() {
        assertThat(lockProvider).isInstanceOf(JdbcTemplateLockProvider.class);
    }
}
