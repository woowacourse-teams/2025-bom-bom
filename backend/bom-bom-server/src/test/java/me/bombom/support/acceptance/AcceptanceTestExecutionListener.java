package me.bombom.support.acceptance;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import me.bombom.support.ResettableTestDouble;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;

public class AcceptanceTestExecutionListener extends AbstractTestExecutionListener {

    private static final int ORDER_BEFORE_TRANSACTION = 3000;

    @Override
    public int getOrder() {
        return ORDER_BEFORE_TRANSACTION;
    }

    @Override
    public void beforeTestClass(TestContext testContext) {
        loadDataSet(testContext);
    }

    @Override
    public void beforeTestMethod(TestContext testContext) {
        testContext.getApplicationContext()
                .getBeansOfType(ResettableTestDouble.class)
                .values()
                .forEach(ResettableTestDouble::reset);

        RestAssuredMockMvc.reset();
        RestAssuredMockMvc.mockMvc(testContext.getApplicationContext().getBean(MockMvc.class));
        RestAssuredMockMvc.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Override
    public void afterTestMethod(TestContext testContext) {
        if (AnnotatedElementUtils.findMergedAnnotation(
                testContext.getTestMethod(),
                ResetsAcceptanceData.class
        ) != null) {
            loadDataSet(testContext);
        }
    }

    private void loadDataSet(TestContext testContext) {
        AcceptanceTest acceptanceTest = AnnotatedElementUtils.findMergedAnnotation(
                testContext.getTestClass(),
                AcceptanceTest.class
        );
        if (acceptanceTest == null) {
            throw new IllegalStateException("@AcceptanceTest 애노테이션을 찾을 수 없습니다.");
        }

        testContext.getApplicationContext()
                .getBean(AcceptanceDataSetLoader.class)
                .load(acceptanceTest.value());
    }
}
