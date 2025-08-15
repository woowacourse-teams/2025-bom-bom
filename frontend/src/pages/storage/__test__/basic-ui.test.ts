import { test, expect } from '@playwright/test';

test.describe('Storage 페이지 - 기본 UI 테스트', () => {
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

  test('페이지 제목과 기본 헤더가 표시되어야 한다', async ({ page }) => {
    await expect(
      page.getByRole('heading', { name: '뉴스레터 보관함' }),
    ).toBeVisible();

    await expect(page.locator('svg').first()).toBeVisible();
  });

  test('뉴스레터 필터 탭들이 표시되어야 한다', async ({ page }) => {
    await expect(page.getByText('전체')).toBeVisible();
    await expect(page.getByText('테크뉴스')).toBeVisible();
    await expect(page.getByText('개발자뉴스')).toBeVisible();
    await expect(page.getByText('AI뉴스')).toBeVisible();

    await expect(page.getByText('3').first()).toBeVisible();
    await expect(page.getByText('5').first()).toBeVisible();
  });

  test('검색 및 정렬 컨트롤이 표시되어야 한다', async ({ page }) => {
    await expect(page.getByRole('searchbox', { name: '검색' })).toBeVisible();

    await expect(
      page.locator('p').filter({ hasText: '최신순' }).first(),
    ).toBeVisible();

    await expect(page.getByText(/총 \d+개/)).toBeVisible();
  });

  test('기사 목록이 표시되어야 한다', async ({ page }) => {
    await expect(page.getByRole('list')).toBeVisible();

    const articleItems = page.getByRole('listitem');
    await expect(articleItems.first()).toBeVisible();

    await expect(page.getByRole('heading', { level: 2 }).first()).toBeVisible();
  });

  test('기사 메타 정보가 표시되어야 한다', async ({ page }) => {
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    const firstArticle = articleList.locator('li').first();
    await expect(firstArticle.getByText('from UPPITY')).toBeVisible();

    await expect(firstArticle.getByText('2025.07.01')).toBeVisible();

    await expect(firstArticle.getByText('5분')).toBeVisible();

    await expect(firstArticle.getByAltText('아티클 썸네일')).toBeVisible();
  });

  test('읽음 상태가 표시되어야 한다', async ({ page }) => {
    await expect(page.getByText('읽음')).toBeVisible();
  });
});
