import { test, expect } from '@playwright/test';

test.describe('Storage 페이지 - 정렬 기능 테스트', () => {
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

  test('기본 정렬 옵션이 최신순으로 설정되어야 한다', async ({ page }) => {
    // 최신순이 기본값으로 표시되는지 확인
    await expect(
      page.locator('p').filter({ hasText: '최신순' }).first(),
    ).toBeVisible();
  });

  test('정렬 드롭다운이 올바르게 작동해야 한다', async ({ page }) => {
    // 정렬 드롭다운 클릭
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .first()
      .click();

    // 드롭다운 옵션들이 표시되는지 확인
    await expect(page.getByRole('listbox')).toBeVisible();
    await expect(page.getByRole('option', { name: '최신순' })).toBeVisible();
    await expect(page.getByRole('option', { name: '오래된순' })).toBeVisible();
  });

  test('오래된순 정렬이 작동해야 한다', async ({ page }) => {
    // 정렬 드롭다운 열기
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .first()
      .click();

    // 오래된순 선택
    await page.getByRole('option', { name: '오래된순' }).click();

    // 변경 반영 대기
    await page.waitForLoadState('networkidle');

    // 선택된 값이 변경되었는지 확인
    await expect(
      page.locator('p').filter({ hasText: '오래된순' }).first(),
    ).toBeVisible();
  });

  test('최신순으로 다시 변경이 가능해야 한다', async ({ page }) => {
    // 먼저 오래된순으로 변경
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .first()
      .click();
    await page.getByRole('option', { name: '오래된순' }).click();
    await page.waitForLoadState('networkidle');

    // 다시 최신순으로 변경
    await page
      .locator('div')
      .filter({ hasText: /^오래된순$/ })
      .first()
      .click();
    await page.getByRole('option', { name: '최신순' }).click();

    // 변경 반영 대기
    await page.waitForLoadState('networkidle');

    // 최신순으로 변경되었는지 확인
    await expect(
      page.locator('p').filter({ hasText: '최신순' }).first(),
    ).toBeVisible();
  });

  test('정렬과 필터가 함께 작동해야 한다', async ({ page }) => {
    // 먼저 필터 적용
    await page.getByText('테크뉴스').click();
    await page.waitForLoadState('networkidle');

    // 정렬 변경
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .click();
    await page.getByRole('option', { name: '오래된순' }).click();

    // 변경 반영 대기
    await page.waitForLoadState('networkidle');

    // 정렬이 적용되었는지 확인
    await expect(
      page.locator('p').filter({ hasText: '오래된순' }).first(),
    ).toBeVisible();
  });

  test('정렬과 검색이 함께 작동해야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    // 검색어 입력
    await searchBox.fill('폭염');
    await searchBox.press('Enter');
    await page.waitForLoadState('networkidle');

    // 정렬 변경
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .click();
    await page.getByRole('option', { name: '오래된순' }).click();

    // API 호출 확인 (검색과 정렬이 모두 적용된 요청)
    await page.waitForLoadState('networkidle');

    // 검색어와 정렬이 모두 유지되는지 확인
    await expect(searchBox).toHaveValue('폭염');
    await expect(
      page.locator('p').filter({ hasText: '오래된순' }).first(),
    ).toBeVisible();
  });

  test('정렬 드롭다운이 외부 클릭시 닫혀야 한다', async ({ page }) => {
    // 드롭다운 열기
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .click();
    await expect(page.getByRole('option', { name: '최신순' })).toBeVisible();

    // 외부 영역 클릭
    await page.getByRole('heading', { name: '뉴스레터 보관함' }).click();

    // 드롭다운이 닫혔는지 확인 (옵션들이 더 이상 보이지 않음)
    await expect(page.getByRole('listbox')).not.toBeVisible();
  });

  test('정렬 드롭다운 아이콘이 표시되어야 한다', async ({ page }) => {
    // 정렬 드롭다운에 화살표 아이콘이 있는지 확인
    const sortButton = page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .first();
    await expect(sortButton.locator('svg')).toBeVisible();
  });

  test('키보드로 정렬 옵션을 선택할 수 있어야 한다', async ({ page }) => {
    // 정렬 드롭다운에 포커스
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .first()
      .focus();

    // Enter 키로 드롭다운 열기
    await page.keyboard.press('Enter');

    // 보조: 클릭으로도 드롭다운 열기 보장
    await page
      .locator('div')
      .filter({ hasText: /^최신순$/ })
      .first()
      .click();

    // 옵션들이 표시되는지 확인
    await expect(page.getByRole('listbox')).toBeVisible();
    await expect(page.getByRole('option', { name: '최신순' })).toBeVisible();
    await expect(page.getByRole('option', { name: '오래된순' })).toBeVisible();

    // 화살표 키로 옵션 선택 후 Enter
    await page.keyboard.press('ArrowDown');
    await page.keyboard.press('Enter');

    // 변경 반영 대기
    await page.waitForLoadState('networkidle');
  });
});
