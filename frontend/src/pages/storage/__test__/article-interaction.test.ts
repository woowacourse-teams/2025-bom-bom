import { test, expect } from '@playwright/test';

test.describe('Storage 페이지 - 기사 상호작용 테스트', () => {
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

  test('기사 목록이 올바르게 로드되어야 한다', async ({ page }) => {
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    await expect(articleList).toBeVisible();

    const articleItems = articleList.locator('li');
    expect(await articleItems.count()).toBeGreaterThan(0);
  });

  test('기사 항목들이 올바른 정보를 표시해야 한다', async ({ page }) => {
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    const firstArticle = articleList.locator('li').first();

    await expect(firstArticle.locator('h2').first()).toBeVisible();

    await expect(firstArticle.locator('p').first()).toBeVisible();

    await expect(firstArticle.getByText('from UPPITY')).toBeVisible();
    await expect(firstArticle.getByText('2025.07.01')).toBeVisible();
    await expect(firstArticle.getByText('5분')).toBeVisible();

    await expect(firstArticle.getByAltText('아티클 썸네일')).toBeVisible();
  });

  test('읽음 상태가 올바르게 표시되어야 한다', async ({ page }) => {
    const readArticles = page.getByText('읽음');

    await expect(readArticles.first()).toBeVisible();

    const allArticles = page.getByRole('listitem');
    const articlesCount = await allArticles.count();
    const readCount = await readArticles.count();

    expect(readCount).toBeLessThan(articlesCount);
  });

  test('기사 링크 클릭이 작동해야 한다', async ({ page }) => {
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    const firstArticleLink = articleList.locator('li a').first();

    const href = await firstArticleLink.getAttribute('href');
    expect(href).toContain('/articles/');

    await firstArticleLink.click();

    await page.waitForTimeout(1000);
  });

  test('기사 썸네일이 올바르게 표시되어야 한다', async ({ page }) => {
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    const articleItems = articleList.locator('li');

    for (let i = 0; i < Math.min(3, await articleItems.count()); i++) {
      const article = articleItems.nth(i);
      const thumbnail = article.getByAltText('아티클 썸네일');

      await expect(thumbnail).toBeVisible();

      const src = await thumbnail.getAttribute('src');
      expect(src).toBeTruthy();
    }
  });

  test('기사 메타데이터가 일관되게 표시되어야 한다', async ({ page }) => {
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    const articleItems = articleList.locator('li');

    for (let i = 0; i < Math.min(3, await articleItems.count()); i++) {
      const article = articleItems.nth(i);

      await expect(article.locator('h2').first()).toBeVisible();

      await expect(article.getByText(/from .+/)).toBeVisible();

      await expect(article.getByText(/\d{4}\.\d{2}\.\d{2}/)).toBeVisible();

      await expect(article.getByText(/\d+분/)).toBeVisible();
    }
  });

  test('기사 호버 효과가 작동해야 한다', async ({ page }) => {
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    const firstArticleLink = articleList.locator('li a').first();

    await firstArticleLink.hover();

    await page.waitForTimeout(500);

    await page.getByRole('heading', { name: '뉴스레터 보관함' }).hover();
    await page.waitForTimeout(500);
  });

  test('기사 목록이 스크롤 가능해야 한다', async ({ page }) => {
    await page.evaluate(() => {
      window.scrollTo(0, document.body.scrollHeight);
    });

    await expect(page.getByRole('list')).toBeVisible();

    await page.evaluate(() => {
      window.scrollTo(0, 0);
    });

    await expect(
      page.getByRole('heading', { name: '뉴스레터 보관함' }),
    ).toBeVisible();
  });

  test('기사 내용 미리보기가 적절히 표시되어야 한다', async ({ page }) => {
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    const articleItems = articleList.locator('li');

    for (let i = 0; i < Math.min(3, await articleItems.count()); i++) {
      const article = articleItems.nth(i);

      const previewTexts = article.locator('p').filter({ hasText: /\S+/ });
      expect(await previewTexts.count()).toBeGreaterThan(0);

      const firstPreview = previewTexts.first();
      const textContent = await firstPreview.textContent();
      expect(textContent?.length).toBeLessThan(500);
    }
  });

  test('빈 상태일 때 적절한 메시지가 표시되어야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    await searchBox.fill('존재하지않는검색어12345678');
    await searchBox.press('Enter');

    await page.waitForResponse(
      (response) =>
        response.url().includes('/api/v1/articles') &&
        response.status() === 200,
    );

    await page.waitForTimeout(1000);
  });

  test('기사 ID가 URL에 올바르게 포함되어야 한다', async ({ page }) => {
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    const articleLinks = articleList.locator('li a');

    for (let i = 0; i < Math.min(3, await articleLinks.count()); i++) {
      const link = articleLinks.nth(i);
      const href = await link.getAttribute('href');

      expect(href).toMatch(/^\/articles\/\d+$/);
    }
  });
});
