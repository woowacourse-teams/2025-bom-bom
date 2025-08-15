import { test, expect } from '@playwright/test';

test.describe('보관함 페이지 - 정렬 기능', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:3000/storage');
    await page.waitForLoadState('networkidle');
  });

  test('기본 정렬은 최신순이다', async ({ page }) => {
    // 정렬 드롭다운에 "최신순"이 표시되는지 확인
    await expect(page.getByText('최신순').first()).toBeVisible();

    // 첫 번째 아티클이 가장 최신 날짜인지 확인 (2025.07.07)
    const firstArticle = page.getByRole('listitem').filter({ hasText: 'from' }).first();
    await expect(firstArticle.getByText('2025.07.07')).toBeVisible();
    await expect(
      firstArticle.getByText('테크뉴스: 이번 주 IT 핫이슈').first(),
    ).toBeVisible();
  });

  test('오래된순 정렬이 정상 작동한다', async ({ page }) => {
    // 정렬 드롭다운 클릭
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .first()
      .click();

    // 오래된순 선택
    await page.getByRole('option', { name: '오래된순' }).click();
    await page.waitForLoadState('networkidle');

    // 정렬 드롭다운에 "오래된순"이 표시되는지 확인
    await expect(page.getByText('오래된순').first()).toBeVisible();

    // 첫 번째 아티클이 가장 오래된 날짜인지 확인 (2025.07.01)
    const firstArticle = page.getByRole('listitem').filter({ hasText: 'from' }).first();
    await expect(firstArticle.getByText('2025.07.01')).toBeVisible();
    await expect(
      firstArticle.getByText('폭염에도 전력난 없는 이유는? 1').first(),
    ).toBeVisible();
  });

  test('최신순으로 다시 정렬이 가능하다', async ({ page }) => {
    // 먼저 오래된순으로 정렬
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .click();
    await page.getByRole('option', { name: '오래된순' }).click();
    await page.waitForLoadState('networkidle');

    // 다시 최신순으로 정렬
    await page
      .locator('div')
      .filter({ hasText: /^오래된순$/ })
      .click();
    await page.getByRole('option', { name: '최신순' }).click();
    await page.waitForLoadState('networkidle');

    // 최신순 정렬 확인
    await expect(page.getByText('최신순').first()).toBeVisible();
    const firstArticle = page.getByRole('listitem').filter({ hasText: 'from' }).first();
    await expect(firstArticle.getByText('2025.07.07')).toBeVisible();
  });

  test('정렬 상태가 검색과 함께 유지된다', async ({ page }) => {
    // 오래된순으로 정렬
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .click();
    await page.getByRole('option', { name: '오래된순' }).click();
    await page.waitForLoadState('networkidle');

    // 검색 수행
    const searchInput = page.getByRole('searchbox', { name: '검색' });
    await searchInput.fill('UPPITY');
    await page.waitForTimeout(2000);

    // 정렬 상태가 유지되는지 확인
    await expect(page.getByText('오래된순').first()).toBeVisible();

    // UPPITY 아티클들이 오래된순으로 정렬되어 있는지 확인
    const firstArticle = page.getByRole('listitem').filter({ hasText: 'from' }).first();
    await expect(firstArticle.getByText('2025.07.01')).toBeVisible();
  });

  test('정렬 상태가 뉴스레터 필터와 함께 유지된다', async ({ page }) => {
    // 오래된순으로 정렬
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .click();
    await page.getByRole('option', { name: '오래된순' }).click();
    await page.waitForLoadState('networkidle');

    // UPPITY 뉴스레터 필터 선택
    await page.getByText('UPPITY3').click();
    await page.waitForLoadState('networkidle');

    // 정렬 상태가 유지되는지 확인
    await expect(page.getByText('오래된순').first()).toBeVisible();

    // UPPITY 아티클들이 오래된순으로 정렬되어 있는지 확인
    const firstArticle = page.getByRole('listitem').filter({ hasText: 'from' }).first();
    await expect(firstArticle.getByText('2025.07.01')).toBeVisible();
  });

  test('정렬 옵션이 올바르게 표시된다', async ({ page }) => {
    // 정렬 드롭다운 클릭
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .click();

    // 정렬 옵션들이 표시되는지 확인
    await expect(page.getByRole('option', { name: '최신순' })).toBeVisible();
    await expect(page.getByRole('option', { name: '오래된순' })).toBeVisible();

    // 정렬 옵션이 2개만 있는지 확인
    const options = page.getByRole('option');
    await expect(options).toHaveCount(2);
  });
});
