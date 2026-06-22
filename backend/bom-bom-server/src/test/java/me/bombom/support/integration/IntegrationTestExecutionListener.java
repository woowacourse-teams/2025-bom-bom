package me.bombom.support.integration;

import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.persistence.CleanUp;
import me.bombom.support.testdouble.ResettableTestDouble;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * 통합 테스트 실행 전 테스트 대역을 초기화하고 인수 테스트가 아닌 경우 DB를 정리한다.
 */
public class IntegrationTestExecutionListener extends AbstractTestExecutionListener {

    private static final int ORDER_BEFORE_TRANSACTION = 3000;

    @Override
    public int getOrder() {
        return ORDER_BEFORE_TRANSACTION;
    }

    @Override
    public void beforeTestMethod(TestContext testContext) {
        testContext.getApplicationContext()
                .getBeansOfType(ResettableTestDouble.class)
                .values()
                .forEach(ResettableTestDouble::reset);

        if (isAcceptanceTest(testContext)) {
            return;
        }
        // CleanUp은 IntegrationTestConfig가 항상 등록한다. 없으면 테스트 설정 오류이므로 일부러 즉시 실패시킨다.
        testContext.getApplicationContext()
                .getBean(CleanUp.class)
                .all();
    }

    private static boolean isAcceptanceTest(TestContext testContext) {
        return AnnotatedElementUtils.findMergedAnnotation(
                testContext.getTestClass(),
                AcceptanceTest.class
        ) != null;
    }
}
