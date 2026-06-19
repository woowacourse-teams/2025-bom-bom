package me.bombom.support;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

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
    }
}
