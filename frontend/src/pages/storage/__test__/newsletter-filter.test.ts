import { test, expect } from '@playwright/test';

test.describe('보관함 페이지 - 뉴스레터 필터링', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/storage');
  });

  test('전체 탭 선택 시 모든 뉴스레터가 표시된다', async ({ page }) => {
    await page.getByText('전체7').click();

    await expect(page.getByText('총 7개')).toBeVisible();

    const articleList = page.getByRole('list');

    await expect(articleList).toBeVisible();

    await expect(page.getByText('from 테크뉴스').first()).toBeVisible();
    await expect(page.getByText('from UPPITY').first()).toBeVisible();
    await expect(page.getByText('from AI뉴스').first()).toBeVisible();
  });

  test('UPPITY 뉴스레터 필터링이 정상 작동한다', async ({ page }) => {
    await page.getByText('UPPITY3').click();

    await expect(page.getByText('총 3개')).toBeVisible();

    const uppityArticles = page.getByText('from UPPITY');

    await expect(uppityArticles).toHaveCount(3);
    await expect(page.getByText('from 테크뉴스')).not.toBeVisible();
    await expect(page.getByText('from AI뉴스')).not.toBeVisible();
  });

  test('AI뉴스 뉴스레터 필터링이 정상 작동한다', async ({ page }) => {
    await page.getByText('AI뉴스1').click();

    await expect(page.getByText('총 1개')).toBeVisible();
    await expect(page.getByText('from AI뉴스')).toBeVisible();
    await expect(page.getByText('AI가 바꿀 미래의 일자리')).toBeVisible();
  });

  test('뉴스레터 탭에 올바른 개수가 표시된다', async ({ page }) => {
    await expect(page.getByText('전체7')).toBeVisible();
    await expect(page.getByText('UPPITY3')).toBeVisible();
    await expect(page.getByText('AI뉴스1')).toBeVisible();
    await expect(page.getByText('스타트업뉴스1')).toBeVisible();
    await expect(page.getByText('개발자뉴스1')).toBeVisible();
    await expect(page.getByText('테크뉴스1')).toBeVisible();
  });
});
