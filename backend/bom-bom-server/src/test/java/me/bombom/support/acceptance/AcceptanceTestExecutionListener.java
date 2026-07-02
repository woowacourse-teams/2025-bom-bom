package me.bombom.support.acceptance;

import io.restassured.RestAssured;
import me.bombom.support.testdouble.ResettableTestDouble;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * 인수 테스트 실행 전후로 데이터셋, 테스트 대역, RestAssured HTTP 상태를 준비하고 복원한다.
 */
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
        resetTestDoubles(testContext);

        RestAssured.reset();
        RestAssured.port = port(testContext);
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        AdditionalAcceptanceDataSet additionalDataSet = additionalDataSet(testContext);
        if (additionalDataSet != null) {
            dataSetLoader(testContext).append(additionalDataSet.value());
        }
    }

    @Override
    public void afterTestMethod(TestContext testContext) {
        AdditionalAcceptanceDataSet additionalDataSet = additionalDataSet(testContext);
        if (additionalDataSet != null) {
            dataSetLoader(testContext).clear(additionalDataSet.value());
            loadDataSet(testContext);
            return;
        }

        if (AnnotatedElementUtils.findMergedAnnotation(
                testContext.getTestMethod(),
                ResetsAcceptanceData.class
        ) != null) {
            loadDataSet(testContext);
        }
    }

    private void loadDataSet(TestContext testContext) {
        resetTestDoubles(testContext);

        AcceptanceTest acceptanceTest = AnnotatedElementUtils.findMergedAnnotation(
                testContext.getTestClass(),
                AcceptanceTest.class
        );
        if (acceptanceTest == null) {
            throw new IllegalStateException("@AcceptanceTest 애노테이션을 찾을 수 없습니다.");
        }

        dataSetLoader(testContext).load(acceptanceTest.value());
    }

    private void resetTestDoubles(TestContext testContext) {
        testContext.getApplicationContext()
                .getBeansOfType(ResettableTestDouble.class)
                .values()
                .forEach(ResettableTestDouble::reset);
    }

    private AdditionalAcceptanceDataSet additionalDataSet(TestContext testContext) {
        return AnnotatedElementUtils.findMergedAnnotation(
                testContext.getTestMethod(),
                AdditionalAcceptanceDataSet.class
        );
    }

    private AcceptanceDataSetLoader dataSetLoader(TestContext testContext) {
        return testContext.getApplicationContext().getBean(AcceptanceDataSetLoader.class);
    }

    private int port(TestContext testContext) {
        return ((WebServerApplicationContext) testContext.getApplicationContext())
                .getWebServer()
                .getPort();
    }
}
