import { test, expect } from '@playwright/test';

test.describe('Storage 페이지 - 뉴스레터 필터 테스트', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/storage');
    await page.waitForLoadState('networkidle');

    const currentUrl = page.url();
    if (currentUrl.includes('/recommend') || currentUrl.includes('/login')) {
      await page.goto('/storage');
      await page.waitForLoadState('networkidle');
    }

    await expect(page.getByRole('list')).toBeVisible();
    await expect(page.getByRole('listitem').first()).toBeVisible();
  });

  test('전체 필터가 기본 선택되어야 한다', async ({ page }) => {
    await expect(page.getByText('전체')).toBeVisible();

    await expect(page.getByText('3').first()).toBeVisible();
  });

  test('테크뉴스 필터 클릭이 작동해야 한다', async ({ page }) => {
    await page.getByText('테크뉴스').click();
    await page.waitForLoadState('networkidle');

    // 필터 적용 후 카드들의 from 라벨이 모두 테크뉴스인지 확인
    const labels = await page
      .locator('ul li')
      .locator('span', { hasText: /^from\s+/ })
      .allTextContents();
    for (const label of labels) {
      expect(label).toContain('테크뉴스');
    }
  });

  test('개발자뉴스 필터 클릭이 작동해야 한다', async ({ page }) => {
    await page.getByText('개발자뉴스').click();
    await page.waitForLoadState('networkidle');

    const labels = await page
      .locator('ul li')
      .locator('span', { hasText: /^from\s+/ })
      .allTextContents();
    for (const label of labels) {
      expect(label).toContain('개발자뉴스');
    }
  });

  test('AI뉴스 필터 클릭이 작동해야 한다', async ({ page }) => {
    await page.getByText('AI뉴스').click();
    await page.waitForLoadState('networkidle');

    const labels = await page
      .locator('ul li')
      .locator('span', { hasText: /^from\s+/ })
      .allTextContents();
    for (const label of labels) {
      expect(label).toContain('AI뉴스');
    }
  });

  test('필터 간 전환이 원활해야 한다', async ({ page }) => {
    await page.getByText('테크뉴스').click();
    await page.waitForLoadState('networkidle');

    await page.getByText('AI뉴스').click();
    await page.waitForLoadState('networkidle');

    await page.getByText('전체').click();
    await page.waitForLoadState('networkidle');
  });

  test('필터별 카운트가 올바르게 표시되어야 한다', async ({ page }) => {
    const filterItems = page.getByRole('tablist').getByRole('listitem');

    const totalFilter = filterItems.filter({ hasText: '전체' });
    await expect(totalFilter.getByText('3')).toBeVisible();

    const techFilter = filterItems.filter({ hasText: '테크뉴스' });
    await expect(techFilter.getByText('5')).toBeVisible();

    const devFilter = filterItems.filter({ hasText: '개발자뉴스' });
    await expect(devFilter.getByText('3')).toBeVisible();

    const aiFilter = filterItems.filter({ hasText: 'AI뉴스' });
    await expect(aiFilter.getByText('2')).toBeVisible();
  });

  test('필터 아이콘이 표시되어야 한다', async ({ page }) => {
    const filterItems = page.getByRole('tablist').getByRole('listitem');

    for (let i = 0; i < (await filterItems.count()); i++) {
      const item = filterItems.nth(i);
      await expect(item.locator('img').first()).toBeVisible();
    }
  });
});
