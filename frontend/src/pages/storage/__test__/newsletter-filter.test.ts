import { test, expect } from '@playwright/test';

test.describe('보관함 페이지 - 뉴스레터 필터링', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:3000/storage');
    await page.waitForLoadState('networkidle');
  });

  test('전체 탭 선택 시 모든 뉴스레터가 표시된다', async ({ page }) => {
    // 전체 탭 클릭
    await page.getByText('전체7').click();
    await page.waitForLoadState('networkidle');

    // 총 개수 확인
    await expect(page.getByText('총 7개')).toBeVisible();

    // 아티클 목록이 표시되는지 확인
    const articleList = page.getByRole('list');
    await expect(articleList).toBeVisible();

    // 여러 뉴스레터의 아티클이 있는지 확인
    await expect(page.getByText('from 테크뉴스').first()).toBeVisible();
    await expect(page.getByText('from UPPITY').first()).toBeVisible();
    await expect(page.getByText('from AI뉴스').first()).toBeVisible();
  });

  test('UPPITY 뉴스레터 필터링이 정상 작동한다', async ({ page }) => {
    // UPPITY 탭 클릭
    await page.getByText('UPPITY3').click();
    await page.waitForLoadState('networkidle');

    // 필터링된 개수 확인
    await expect(page.getByText('총 3개')).toBeVisible();

    // UPPITY 뉴스레터만 표시되는지 확인
    const uppityArticles = page.getByText('from UPPITY');
    await expect(uppityArticles).toHaveCount(3);

    // 다른 뉴스레터는 표시되지 않는지 확인
    await expect(page.getByText('from 테크뉴스')).not.toBeVisible();
    await expect(page.getByText('from AI뉴스')).not.toBeVisible();
  });

  test('AI뉴스 뉴스레터 필터링이 정상 작동한다', async ({ page }) => {
    // AI뉴스 탭 클릭
    await page.getByText('AI뉴스1').click();
    await page.waitForLoadState('networkidle');

    // 필터링된 개수 확인
    await expect(page.getByText('총 1개')).toBeVisible();

    // AI뉴스 아티클만 표시되는지 확인
    await expect(page.getByText('from AI뉴스')).toBeVisible();
    await expect(page.getByText('AI가 바꿀 미래의 일자리')).toBeVisible();
  });

  test('뉴스레터 탭에 올바른 개수가 표시된다', async ({ page }) => {
    // 각 뉴스레터 탭의 개수 확인
    await expect(page.getByText('전체7')).toBeVisible();
    await expect(page.getByText('UPPITY3')).toBeVisible();
    await expect(page.getByText('AI뉴스1')).toBeVisible();
    await expect(page.getByText('스타트업뉴스1')).toBeVisible();
    await expect(page.getByText('개발자뉴스1')).toBeVisible();
    await expect(page.getByText('테크뉴스1')).toBeVisible();
  });
});
