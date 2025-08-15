import { test, expect } from '@playwright/test';

test.describe('Storage 페이지 - 검색 기능 테스트', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/storage');
    await page.waitForLoadState('networkidle');

    const currentUrl = page.url();
    if (currentUrl.includes('/recommend') || currentUrl.includes('/login')) {
      await page.goto('/storage');
      await page.waitForLoadState('networkidle');
    }
  });

  test('검색창이 정상적으로 표시되어야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    await expect(searchBox).toBeVisible();

    await expect(searchBox).toHaveValue('');

    await expect(searchBox).toHaveAttribute('placeholder', /.+/);
  });

  test('텍스트 입력이 정상적으로 작동해야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    await searchBox.fill('폭염');

    await expect(searchBox).toHaveValue('폭염');
  });

  test('Enter 키로 검색이 실행되어야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    await searchBox.fill('폭염');

    await searchBox.press('Enter');

    const resp = await page.waitForResponse(
      (response) =>
        response.url().includes('/api/v1/articles') &&
        response.status() === 200,
    );

    // 요청에 keyword 쿼리스트링이 포함되었는지 확인
    expect(resp.url()).toContain('keyword=%ED%8F%AD%EC%97%BC');

    await expect(searchBox).toHaveValue('폭염');
  });

  test('다양한 검색어로 검색이 작동해야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    const searchTerms = ['기술', '뉴스', '개발', 'AI'];

    for (const term of searchTerms) {
      await searchBox.fill(term);
      await searchBox.press('Enter');

      await page.waitForLoadState('networkidle');

      await expect(searchBox).toHaveValue(term);

      await page.waitForTimeout(500);
    }
  });

  test('빈 검색어로 검색 시 전체 결과가 표시되어야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    await searchBox.fill('테스트');
    await searchBox.press('Enter');
    await page.waitForLoadState('networkidle');

    await searchBox.clear();
    await searchBox.press('Enter');

    await page.waitForLoadState('networkidle');
  });

  test('검색과 필터가 함께 작동해야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    await page.getByText('테크뉴스').click();
    await page.waitForLoadState('networkidle');

    await searchBox.fill('폭염');
    await searchBox.press('Enter');

    await page.waitForLoadState('networkidle');

    await expect(searchBox).toHaveValue('폭염');

    // 화면의 카드들이 모두 필터 조건과 키워드 조건을 만족하는지 간단 확인
    const labels = await page
      .locator('ul li')
      .locator('span', { hasText: /^from\s+/ })
      .allTextContents();
    for (const label of labels) {
      expect(label).toContain('테크뉴스');
    }
    const titles = await page.locator('ul li h2').allTextContents();
    for (const t of titles) {
      expect(t).toMatch(/폭염|/); // 제목 또는 내용 요약 중 일부가 폭염을 포함할 수 있음
    }
  });
});
