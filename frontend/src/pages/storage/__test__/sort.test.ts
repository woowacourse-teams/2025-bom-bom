import { test, expect } from '@playwright/test';

test.describe('보관함 페이지 - 정렬 기능', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/storage');
    await page.waitForLoadState('networkidle');
  });

  test('기본 정렬은 최신순이다', async ({ page }) => {
    await expect(page.getByText('최신순').first()).toBeVisible();

    const firstArticle = page
      .getByRole('listitem')
      .filter({ hasText: 'from' })
      .first();
    await expect(firstArticle.getByText('2025.07.07')).toBeVisible();
    await expect(
      firstArticle.getByText('테크뉴스: 이번 주 IT 핫이슈').first(),
    ).toBeVisible();
  });

  test('오래된순 정렬이 정상 작동한다', async ({ page }) => {
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .first()
      .click();

    await page.getByRole('option', { name: '오래된순' }).click();
    await page.waitForLoadState('networkidle');

    await expect(page.getByText('오래된순').first()).toBeVisible();

    const firstArticle = page
      .getByRole('listitem')
      .filter({ hasText: 'from' })
      .first();
    await expect(firstArticle.getByText('2025.07.01')).toBeVisible();
    await expect(
      firstArticle.getByText('폭염에도 전력난 없는 이유는? 1').first(),
    ).toBeVisible();
  });

  test('최신순으로 다시 정렬이 가능하다', async ({ page }) => {
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .click();
    await page.getByRole('option', { name: '오래된순' }).click();
    await page.waitForLoadState('networkidle');

    await page
      .locator('div')
      .filter({ hasText: /^오래된순$/ })
      .click();
    await page.getByRole('option', { name: '최신순' }).click();
    await page.waitForLoadState('networkidle');

    await expect(page.getByText('최신순').first()).toBeVisible();
    const firstArticle = page
      .getByRole('listitem')
      .filter({ hasText: 'from' })
      .first();
    await expect(firstArticle.getByText('2025.07.07')).toBeVisible();
  });

  test('정렬 상태가 검색과 함께 유지된다', async ({ page }) => {
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .click();
    await page.getByRole('option', { name: '오래된순' }).click();
    await page.waitForLoadState('networkidle');

    const searchInput = page.getByRole('searchbox', { name: '검색' });
    await searchInput.fill('UPPITY');
    await page.waitForTimeout(2000);

    await expect(page.getByText('오래된순').first()).toBeVisible();

    const firstArticle = page
      .getByRole('listitem')
      .filter({ hasText: 'from' })
      .first();
    await expect(firstArticle.getByText('2025.07.01')).toBeVisible();
  });

  test('정렬 상태가 뉴스레터 필터와 함께 유지된다', async ({ page }) => {
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .click();
    await page.getByRole('option', { name: '오래된순' }).click();
    await page.waitForLoadState('networkidle');

    await page.getByText('UPPITY3').click();
    await page.waitForLoadState('networkidle');

    await expect(page.getByText('오래된순').first()).toBeVisible();

    const firstArticle = page
      .getByRole('listitem')
      .filter({ hasText: 'from' })
      .first();
    await expect(firstArticle.getByText('2025.07.01')).toBeVisible();
  });

  test('정렬 옵션이 올바르게 표시된다', async ({ page }) => {
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .click();

    await expect(page.getByRole('option', { name: '최신순' })).toBeVisible();
    await expect(page.getByRole('option', { name: '오래된순' })).toBeVisible();

    const options = page.getByRole('option');
    await expect(options).toHaveCount(2);
  });
});
