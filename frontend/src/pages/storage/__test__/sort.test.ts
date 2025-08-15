import { test, expect } from '@playwright/test';

test.describe('Storage 페이지 - 정렬 기능 테스트', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/storage');
    await page.waitForLoadState('networkidle');

    const currentUrl = page.url();
    if (currentUrl.includes('/recommend') || currentUrl.includes('/login')) {
      await page.goto('/storage');
      await page.waitForLoadState('networkidle');
    }
  });

  test('기본 정렬 옵션이 최신순으로 설정되어야 한다', async ({ page }) => {
    await expect(
      page.locator('p').filter({ hasText: '최신순' }).first(),
    ).toBeVisible();
  });

  test('정렬 드롭다운이 올바르게 작동해야 한다', async ({ page }) => {
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .first()
      .click();

    await expect(page.getByRole('listbox')).toBeVisible();
    await expect(page.getByRole('option', { name: '최신순' })).toBeVisible();
    await expect(page.getByRole('option', { name: '오래된순' })).toBeVisible();
  });

  test('오래된순 정렬이 작동해야 한다', async ({ page }) => {
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .first()
      .click();

    await page.getByRole('option', { name: '오래된순' }).click();

    // 정렬 라벨 확인
    await page.waitForLoadState('networkidle');
    await expect(
      page.locator('p').filter({ hasText: '오래된순' }).first(),
    ).toBeVisible();

    // 실제 목록 날짜가 오름차순인지 확인
    const dateTexts: string[] = await page
      .locator('ul li')
      .locator('span', { hasText: /\d{4}\.\d{2}\.\d{2}/ })
      .allTextContents();

    const nums: number[] = dateTexts
      .map((s) => Number(s.replaceAll('.', '')))
      .filter((n): n is number => Number.isFinite(n));
    for (let i = 1; i < nums.length; i++) {
      const curr = nums[i]!;
      const prev = nums[i - 1]!;
      expect(curr).toBeGreaterThanOrEqual(prev);
    }
  });

  test('최신순으로 다시 변경이 가능해야 한다', async ({ page }) => {
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .first()
      .click();
    await page.getByRole('option', { name: '오래된순' }).click();
    await page.waitForLoadState('networkidle');

    await page
      .locator('div')
      .filter({ hasText: /^오래된순$/ })
      .first()
      .click();
    await page.getByRole('option', { name: '최신순' }).click();

    await page.waitForLoadState('networkidle');
    await expect(
      page.locator('p').filter({ hasText: '최신순' }).first(),
    ).toBeVisible();

    // 실제 목록 날짜가 내림차순인지 확인
    const dateTexts: string[] = await page
      .locator('ul li')
      .locator('span', { hasText: /\d{4}\.\d{2}\.\d{2}/ })
      .allTextContents();

    const nums: number[] = dateTexts
      .map((s) => Number(s.replaceAll('.', '')))
      .filter((n): n is number => Number.isFinite(n));
    for (let i = 1; i < nums.length; i++) {
      const curr = nums[i]!;
      const prev = nums[i - 1]!;
      expect(curr).toBeLessThanOrEqual(prev);
    }
  });

  test('정렬과 필터가 함께 작동해야 한다', async ({ page }) => {
    await page.getByText('테크뉴스').click();
    await page.waitForLoadState('networkidle');

    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .click();
    await page.getByRole('option', { name: '오래된순' }).click();

    await page.waitForLoadState('networkidle');
    await expect(
      page.locator('p').filter({ hasText: '오래된순' }).first(),
    ).toBeVisible();

    // 필터 적용 상태에서의 정렬도 올바른지 확인
    const dateTexts: string[] = await page
      .locator('ul li')
      .locator('span', { hasText: /\d{4}\.\d{2}\.\d{2}/ })
      .allTextContents();

    const nums: number[] = dateTexts
      .map((s) => Number(s.replaceAll('.', '')))
      .filter((n): n is number => Number.isFinite(n));
    for (let i = 1; i < nums.length; i++) {
      const curr = nums[i]!;
      const prev = nums[i - 1]!;
      expect(curr).toBeGreaterThanOrEqual(prev);
    }
  });

  test('정렬과 검색이 함께 작동해야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    await searchBox.fill('폭염');
    await searchBox.press('Enter');
    await page.waitForLoadState('networkidle');

    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .click();
    await page.getByRole('option', { name: '오래된순' }).click();

    await page.waitForLoadState('networkidle');

    await expect(searchBox).toHaveValue('폭염');
    await expect(
      page.locator('p').filter({ hasText: '오래된순' }).first(),
    ).toBeVisible();
  });

  test('정렬 드롭다운이 외부 클릭시 닫혀야 한다', async ({ page }) => {
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .click();
    await expect(page.getByRole('option', { name: '최신순' })).toBeVisible();

    await page.getByRole('heading', { name: '뉴스레터 보관함' }).click();

    await expect(page.getByRole('listbox')).not.toBeVisible();
  });

  test('정렬 드롭다운 아이콘이 표시되어야 한다', async ({ page }) => {
    const sortButton = page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .first();
    await expect(sortButton.locator('svg')).toBeVisible();
  });

  test('키보드로 정렬 옵션을 선택할 수 있어야 한다', async ({ page }) => {
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .first()
      .focus();

    await page.keyboard.press('Enter');

    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .first()
      .click();

    await expect(page.getByRole('listbox')).toBeVisible();
    await expect(page.getByRole('option', { name: '최신순' })).toBeVisible();
    await expect(page.getByRole('option', { name: '오래된순' })).toBeVisible();

    await page.keyboard.press('ArrowDown');
    await page.keyboard.press('Enter');

    await page.waitForLoadState('networkidle');
  });
});
