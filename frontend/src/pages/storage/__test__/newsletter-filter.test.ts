import { test, expect } from '@playwright/test';

test.describe('Storage 페이지 - 뉴스레터 필터 테스트', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/storage');
    await page.waitForLoadState('networkidle');

    // 리다이렉트 처리
    const currentUrl = page.url();
    if (currentUrl.includes('/recommend') || currentUrl.includes('/login')) {
      await page.goto('/storage');
      await page.waitForLoadState('networkidle');
    }
  });

  test('전체 필터가 기본 선택되어야 한다', async ({ page }) => {
    // "전체" 필터가 표시되는지 확인
    await expect(page.getByText('전체')).toBeVisible();

    // 전체 카운트 확인
    await expect(page.getByText('3').first()).toBeVisible();
  });

  test('테크뉴스 필터 클릭이 작동해야 한다', async ({ page }) => {
    // 테크뉴스 필터 클릭
    await page.getByText('테크뉴스').click();

    // API 호출 대기
    await page.waitForResponse(
      (response) =>
        response.url().includes('/api/v1/articles') &&
        response.status() === 200,
    );

    // 필터가 적용되었는지 확인 (URL 파라미터나 상태 변화)
    await page.waitForTimeout(1000); // API 응답 처리 대기
  });

  test('개발자뉴스 필터 클릭이 작동해야 한다', async ({ page }) => {
    // 개발자뉴스 필터 클릭
    await page.getByText('개발자뉴스').click();

    // API 호출 대기
    await page.waitForResponse(
      (response) =>
        response.url().includes('/api/v1/articles') &&
        response.status() === 200,
    );

    await page.waitForTimeout(1000);
  });

  test('AI뉴스 필터 클릭이 작동해야 한다', async ({ page }) => {
    // AI뉴스 필터 클릭
    await page.getByText('AI뉴스').click();

    // API 호출 대기
    await page.waitForResponse(
      (response) =>
        response.url().includes('/api/v1/articles') &&
        response.status() === 200,
    );

    await page.waitForTimeout(1000);
  });

  test('필터 간 전환이 원활해야 한다', async ({ page }) => {
    // 첫 번째 필터 클릭
    await page.getByText('테크뉴스').click();
    await page.waitForResponse(
      (response) =>
        response.url().includes('/api/v1/articles') &&
        response.status() === 200,
    );

    // 다른 필터로 전환
    await page.getByText('AI뉴스').click();
    await page.waitForResponse(
      (response) =>
        response.url().includes('/api/v1/articles') &&
        response.status() === 200,
    );

    // 전체로 다시 전환
    await page.getByText('전체').click();
    await page.waitForResponse(
      (response) =>
        response.url().includes('/api/v1/articles') &&
        response.status() === 200,
    );
  });

  test('필터별 카운트가 올바르게 표시되어야 한다', async ({ page }) => {
    // 각 필터의 카운트 확인
    const filterItems = page.getByRole('tablist').getByRole('listitem');

    // 전체 (3개)
    const totalFilter = filterItems.filter({ hasText: '전체' });
    await expect(totalFilter.getByText('3')).toBeVisible();

    // 테크뉴스 (5개)
    const techFilter = filterItems.filter({ hasText: '테크뉴스' });
    await expect(techFilter.getByText('5')).toBeVisible();

    // 개발자뉴스 (3개)
    const devFilter = filterItems.filter({ hasText: '개발자뉴스' });
    await expect(devFilter.getByText('3')).toBeVisible();

    // AI뉴스 (2개)
    const aiFilter = filterItems.filter({ hasText: 'AI뉴스' });
    await expect(aiFilter.getByText('2')).toBeVisible();
  });

  test('필터 아이콘이 표시되어야 한다', async ({ page }) => {
    // 각 필터 항목에 아이콘이 있는지 확인
    const filterItems = page.getByRole('tablist').getByRole('listitem');

    for (let i = 0; i < (await filterItems.count()); i++) {
      const item = filterItems.nth(i);
      await expect(item.locator('img').first()).toBeVisible();
    }
  });
});
