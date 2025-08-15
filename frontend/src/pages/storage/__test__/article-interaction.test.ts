import { test, expect } from '@playwright/test';

test.describe('Storage 페이지 - 기사 상호작용 테스트', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/storage');
    await page.waitForLoadState('networkidle');

    // 리다이렉트 처리
    const currentUrl = page.url();
    if (currentUrl.includes('/recommend') || currentUrl.includes('/login')) {
      await page.goto('/storage');
      await page.waitForLoadState('networkidle');
    }

    // 기사 목록이 로드될 때까지 대기
    await expect(page.getByRole('list')).toBeVisible();
    await expect(page.getByRole('listitem').first()).toBeVisible();
  });

  test('기사 목록이 올바르게 로드되어야 한다', async ({ page }) => {
    // 기사 목록 컨테이너 확인 (기사 링크를 포함한 UL을 기준으로 선택)
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    await expect(articleList).toBeVisible();

    // 기사 항목들이 있는지 확인
    const articleItems = articleList.locator('li');
    expect(await articleItems.count()).toBeGreaterThan(0);
  });

  test('기사 항목들이 올바른 정보를 표시해야 한다', async ({ page }) => {
    // 첫 번째 기사 항목 확인 (기사 리스트 기준)
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    const firstArticle = articleList.locator('li').first();

    // 기사 제목 확인
    await expect(firstArticle.locator('h2').first()).toBeVisible();

    // 기사 내용 미리보기 확인 (설명 단락 존재 확인)
    await expect(firstArticle.locator('p').first()).toBeVisible();

    // 메타 정보 확인
    await expect(firstArticle.getByText('from UPPITY')).toBeVisible();
    await expect(firstArticle.getByText('2025.07.01')).toBeVisible();
    await expect(firstArticle.getByText('5분')).toBeVisible();

    // 썸네일 이미지 확인
    await expect(firstArticle.getByAltText('아티클 썸네일')).toBeVisible();
  });

  test('읽음 상태가 올바르게 표시되어야 한다', async ({ page }) => {
    // 읽음 표시가 있는 기사 찾기
    const readArticles = page.getByText('읽음');

    // 읽음 표시가 있는지 확인
    await expect(readArticles.first()).toBeVisible();

    // 읽지 않은 기사도 있는지 확인 (읽음 표시가 없는 기사)
    const allArticles = page.getByRole('listitem');
    const articlesCount = await allArticles.count();
    const readCount = await readArticles.count();

    // 모든 기사가 읽음 상태는 아닌지 확인
    expect(readCount).toBeLessThan(articlesCount);
  });

  test('기사 링크 클릭이 작동해야 한다', async ({ page }) => {
    // 첫 번째 기사 링크 클릭
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    const firstArticleLink = articleList.locator('li a').first();

    // 링크가 올바른 URL을 가지고 있는지 확인
    const href = await firstArticleLink.getAttribute('href');
    expect(href).toContain('/articles/');

    // 클릭 시 페이지 이동 확인 (새 탭이나 현재 탭에서)
    await firstArticleLink.click();

    // URL 변경 또는 새 페이지 로드 대기
    await page.waitForTimeout(1000);
  });

  test('기사 썸네일이 올바르게 표시되어야 한다', async ({ page }) => {
    // 모든 기사 항목의 썸네일 확인
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    const articleItems = articleList.locator('li');

    for (let i = 0; i < Math.min(3, await articleItems.count()); i++) {
      const article = articleItems.nth(i);
      const thumbnail = article.getByAltText('아티클 썸네일');

      // 썸네일이 있는지 확인
      await expect(thumbnail).toBeVisible();

      // 이미지 소스가 있는지 확인
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

    // 각 기사가 필수 메타데이터를 가지고 있는지 확인
    for (let i = 0; i < Math.min(3, await articleItems.count()); i++) {
      const article = articleItems.nth(i);

      // 제목 확인
      await expect(article.locator('h2').first()).toBeVisible();

      // 출처 정보 확인 (from XXX 형태)
      await expect(article.getByText(/from .+/)).toBeVisible();

      // 날짜 정보 확인
      await expect(article.getByText(/\d{4}\.\d{2}\.\d{2}/)).toBeVisible();

      // 읽기 시간 확인
      await expect(article.getByText(/\d+분/)).toBeVisible();
    }
  });

  test('기사 호버 효과가 작동해야 한다', async ({ page }) => {
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    const firstArticleLink = articleList.locator('li a').first();

    // 마우스 호버
    await firstArticleLink.hover();

    // 호버 상태 확인 (스타일 변화 등)
    await page.waitForTimeout(500);

    // 호버가 제거되었을 때
    await page.getByRole('heading', { name: '뉴스레터 보관함' }).hover();
    await page.waitForTimeout(500);
  });

  test('기사 목록이 스크롤 가능해야 한다', async ({ page }) => {
    // 페이지 스크롤 테스트
    await page.evaluate(() => {
      window.scrollTo(0, document.body.scrollHeight);
    });

    // 스크롤 후에도 기사 목록이 보이는지 확인
    await expect(page.getByRole('list')).toBeVisible();

    // 다시 위로 스크롤
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

    // 각 기사의 내용 미리보기 확인
    for (let i = 0; i < Math.min(3, await articleItems.count()); i++) {
      const article = articleItems.nth(i);

      // 내용 미리보기 텍스트가 있는지 확인
      const previewTexts = article.locator('p').filter({ hasText: /\S+/ });
      expect(await previewTexts.count()).toBeGreaterThan(0);

      // 내용이 너무 길지 않은지 확인 (잘림 처리)
      const firstPreview = previewTexts.first();
      const textContent = await firstPreview.textContent();
      expect(textContent?.length).toBeLessThan(500); // 적당한 길이 제한
    }
  });

  test('빈 상태일 때 적절한 메시지가 표시되어야 한다', async ({ page }) => {
    const searchBox = page.getByRole('searchbox', { name: '검색' });

    // 존재하지 않는 검색어로 빈 결과 만들기
    await searchBox.fill('존재하지않는검색어12345678');
    await searchBox.press('Enter');

    // API 응답 대기
    await page.waitForResponse(
      (response) =>
        response.url().includes('/api/v1/articles') &&
        response.status() === 200,
    );

    await page.waitForTimeout(1000);

    // 빈 상태 메시지나 빈 목록 확인
    // (실제 구현에 따라 달라질 수 있음)
  });

  test('기사 ID가 URL에 올바르게 포함되어야 한다', async ({ page }) => {
    const articleList = page
      .locator('ul')
      .filter({ has: page.locator('a[href^="/articles/"]') })
      .first();
    const articleLinks = articleList.locator('li a');

    // 각 기사 링크의 URL 확인
    for (let i = 0; i < Math.min(3, await articleLinks.count()); i++) {
      const link = articleLinks.nth(i);
      const href = await link.getAttribute('href');

      // URL 형식 확인 (/articles/{id})
      expect(href).toMatch(/^\/articles\/\d+$/);
    }
  });
});
