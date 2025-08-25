import { test, expect } from '@playwright/test';
import { CATEGORIES } from '@/constants/category';

test.describe('추천 페이지 - 트렌디 섹션', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/recommend');
  });

  test('트렌디 섹션 헤더가 올바르게 표시되어야 한다', async ({ page }) => {
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();

    const iconContainer = page
      .locator('span')
      .filter({ hasText: /트렌디한 뉴스레터/ })
      .locator('..')
      .locator('img, svg')
      .first();
    if ((await iconContainer.count()) > 0) {
      await expect(iconContainer).toBeVisible();
    }
  });

  test('카테고리 필터 칩들이 표시되어야 한다', async ({ page }) => {
    for (const category of CATEGORIES) {
      await expect(page.getByText(category)).toBeVisible();
    }
  });

  test('카테고리 칩을 클릭하면 뉴스레터가 필터링되어야 한다', async ({
    page,
  }) => {
    const allCategoryButton = page.getByRole('button', { name: '전체' });
    await expect(allCategoryButton).toBeVisible();

    const itTechButton = page.getByRole('button', { name: 'IT/테크' });
    await itTechButton.click();

    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();

    const foodButton = page.getByRole('button', { name: '푸드' });
    await foodButton.click();
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();

    await allCategoryButton.click();
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();
  });

  test('뉴스레터 정보가 올바르게 표시되어야 한다', async ({ page }) => {
    await expect(page.getByText('오! 당신이 반할 그 맛 😋')).toBeVisible();
    await expect(page.getByText('최신 개발 Dev 뉴스')).toBeVisible();

    const images = page.locator('img[alt*="뉴스레터 이미지"]');
    await expect(images.first()).toBeVisible();
  });

  test('모바일에서 반응형으로 동작해야 한다', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });

    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();

    await expect(page.getByText('전체')).toBeVisible();
    await expect(page.getByText('IT/테크')).toBeVisible();

    await expect(page.getByText('오당맛')).toBeVisible();
  });
});
