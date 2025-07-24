import { expect, test } from '@playwright/test';

test.describe('사용자는 기사를 조회할 수 있다.', () => {
  test('페이지에 접속하면 기사 목록이 보여야 한다.', async ({ page }) => {
    await page.goto('/');
    await expect(
      page.locator('[data-testid="article-card"]').first(),
    ).toBeVisible();
  });
});
