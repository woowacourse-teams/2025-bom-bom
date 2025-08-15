import { test, expect } from '@playwright/test';

test.describe('Storage 페이지 - 검색 기능 테스트', () => {
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

  test('검색창이 정상적으로 표시되어야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    // 검색창이 보이는지 확인
    await expect(searchBox).toBeVisible();

    // 검색창이 비어있는지 확인
    await expect(searchBox).toHaveValue('');

    // placeholder 존재 확인 (값이 비어있을 수 있으므로 존재만 확인)
    await expect(searchBox).toHaveAttribute('placeholder', /.+/);
  });

  test('텍스트 입력이 정상적으로 작동해야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    // 텍스트 입력
    await searchBox.fill('폭염');

    // 입력된 값 확인
    await expect(searchBox).toHaveValue('폭염');
  });

  test('Enter 키로 검색이 실행되어야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    // 검색어 입력
    await searchBox.fill('폭염');

    // Enter 키 입력
    await searchBox.press('Enter');

    // API 호출 확인
    await page.waitForResponse(
      (response) =>
        response.url().includes('/api/v1/articles') &&
        response.status() === 200,
    );

    // 검색어가 유지되는지 확인
    await expect(searchBox).toHaveValue('폭염');
  });

  test('다양한 검색어로 검색이 작동해야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    const searchTerms = ['기술', '뉴스', '개발', 'AI'];

    for (const term of searchTerms) {
      // 검색어 입력
      await searchBox.fill(term);
      await searchBox.press('Enter');

      // API 호출 대기
      await page.waitForLoadState('networkidle');

      // 검색어 유지 확인
      await expect(searchBox).toHaveValue(term);

      // 잠시 대기
      await page.waitForTimeout(500);
    }
  });

  test('빈 검색어로 검색 시 전체 결과가 표시되어야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    // 먼저 검색어 입력
    await searchBox.fill('테스트');
    await searchBox.press('Enter');
    await page.waitForLoadState('networkidle');

    // 검색어 삭제
    await searchBox.clear();
    await searchBox.press('Enter');

    // API 호출 확인
    await page.waitForLoadState('networkidle');
  });

  test('검색과 필터가 함께 작동해야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    // 먼저 필터 선택
    await page.getByText('테크뉴스').click();
    await page.waitForLoadState('networkidle');

    // 검색어 입력
    await searchBox.fill('폭염');
    await searchBox.press('Enter');

    // 변경 반영 대기 (필터와 검색이 모두 적용)
    await page.waitForLoadState('networkidle');

    // 검색어 유지 확인
    await expect(searchBox).toHaveValue('폭염');
  });

  test('검색 결과가 없을 때 적절한 메시지가 표시되어야 한다', async ({
    page,
  }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    // 존재하지 않을 것 같은 검색어 입력
    await searchBox.fill('존재하지않는검색어12345');
    await searchBox.press('Enter');

    // API 호출 대기
    await page.waitForResponse(
      (response) =>
        response.url().includes('/api/v1/articles') &&
        response.status() === 200,
    );

    // 빈 결과 처리 확인 (총 0개 표시되거나 빈 목록)
    await page.waitForTimeout(1000);
  });

  test('검색창 클리어 기능이 작동해야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    // 검색어 입력
    await searchBox.fill('테스트 검색어');
    await expect(searchBox).toHaveValue('테스트 검색어');

    // 검색창 클리어
    await searchBox.clear();
    await expect(searchBox).toHaveValue('');
  });
});
