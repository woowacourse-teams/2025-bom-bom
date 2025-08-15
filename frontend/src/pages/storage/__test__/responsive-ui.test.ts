import { test, expect } from '@playwright/test';

test.describe('Storage 페이지 - 반응형 UI 테스트', () => {
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

  test('데스크톱에서 사이드바가 표시되어야 한다', async ({ page }) => {
    // 데스크톱 크기로 설정
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.waitForLoadState('networkidle');
    
    // 사이드바 영역 확인
    await expect(page.getByRole('heading', { name: '뉴스레터', level: 3 })).toBeVisible();
    await expect(page.getByRole('heading', { name: '바로 가기', level: 3 })).toBeVisible();
    
    // 바로 가기 링크들 확인
    await expect(page.getByRole('link', { name: '북마크' })).toBeVisible();
    await expect(page.getByRole('link', { name: '메모' })).toBeVisible();
  });

  test('모바일에서 사이드바가 숨겨져야 한다', async ({ page }) => {
    // 모바일 크기로 설정
    await page.setViewportSize({ width: 375, height: 667 });
    await page.waitForLoadState('networkidle');
    
    // 사이드바가 숨겨졌는지 확인 (존재하지 않거나 보이지 않음)
    await expect(page.getByRole('heading', { name: '뉴스레터', level: 3 })).not.toBeVisible();
    await expect(page.getByRole('heading', { name: '바로 가기', level: 3 })).not.toBeVisible();
    
    // 기본 콘텐츠는 여전히 표시되어야 함
    await expect(page.getByRole('heading', { name: '뉴스레터 보관함' })).toBeVisible();
    await expect(page.getByRole('searchbox', { name: '검색' })).toBeVisible();
  });

  test('데스크톱에서 풀 네비게이션이 표시되어야 한다', async ({ page }) => {
    // 데스크톱 크기로 설정
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.waitForLoadState('networkidle');
    
    // 네비게이션 링크들 확인
    await expect(page.getByRole('link', { name: '오늘의 뉴스레터' })).toBeVisible();
    await expect(page.getByRole('link', { name: '뉴스레터 보관함' })).toBeVisible();
    await expect(page.getByRole('link', { name: '뉴스레터 추천' })).toBeVisible();
    
    // 로고와 설명 텍스트 확인
    await expect(page.getByText('봄봄')).toBeVisible();
    await expect(page.getByText('당신의 하루에 찾아오는 작은 설렘')).toBeVisible();
  });

  test('모바일에서 간소화된 헤더가 표시되어야 한다', async ({ page }) => {
    // 모바일 크기로 설정
    await page.setViewportSize({ width: 375, height: 667 });
    await page.waitForLoadState('networkidle');
    
    // 기본 로고는 표시되어야 함
    await expect(page.getByText('봄봄')).toBeVisible();
    
    // 로그인 버튼 확인
    await expect(page.getByRole('button', { name: '로그인' })).toBeVisible();
    
    // 하단 네비게이션 확인
    const navigationLinks = page.getByRole('navigation').getByRole('link');
    await expect(navigationLinks.first()).toBeVisible();
  });

  test('모바일에서 컨텐츠 레이아웃이 세로로 배치되어야 한다', async ({ page }) => {
    // 모바일 크기로 설정
    await page.setViewportSize({ width: 375, height: 667 });
    await page.waitForLoadState('networkidle');
    
    // 뉴스레터 필터가 상단에 표시
    await expect(page.getByText('전체')).toBeVisible();
    await expect(page.getByText('테크뉴스')).toBeVisible();
    
    // 검색 및 정렬이 그 아래 표시
    await expect(page.getByRole('searchbox', { name: '검색' })).toBeVisible();
    await expect(page.getByText('최신순')).toBeVisible();
    
    // 기사 목록이 마지막에 표시
    await expect(page.getByRole('list')).toBeVisible();
  });

  test('태블릿 크기에서도 적절히 반응해야 한다', async ({ page }) => {
    // 태블릿 크기로 설정
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.waitForLoadState('networkidle');
    
    // 주요 요소들이 여전히 표시되는지 확인
    await expect(page.getByRole('heading', { name: '뉴스레터 보관함' })).toBeVisible();
    await expect(page.getByRole('searchbox', { name: '검색' })).toBeVisible();
    await expect(page.getByRole('list')).toBeVisible();
    
    // 네비게이션이 적절히 표시되는지 확인
    await expect(page.getByRole('link', { name: '오늘의 뉴스레터' })).toBeVisible();
  });

  test('화면 크기 변경 시 즉시 반응해야 한다', async ({ page }) => {
    // 데스크톱으로 시작
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.waitForLoadState('networkidle');
    
    // 사이드바가 있는지 확인
    await expect(page.getByRole('heading', { name: '바로 가기', level: 3 })).toBeVisible();
    
    // 모바일로 변경
    await page.setViewportSize({ width: 375, height: 667 });
    await page.waitForTimeout(500); // 레이아웃 변경 대기
    
    // 사이드바가 숨겨졌는지 확인
    await expect(page.getByRole('heading', { name: '바로 가기', level: 3 })).not.toBeVisible();
    
    // 다시 데스크톱으로 변경
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.waitForTimeout(500);
    
    // 사이드바가 다시 나타났는지 확인
    await expect(page.getByRole('heading', { name: '바로 가기', level: 3 })).toBeVisible();
  });

  test('모든 화면 크기에서 기본 기능이 작동해야 한다', async ({ page }) => {
    const viewports = [
      { width: 320, height: 568 },  // 작은 모바일
      { width: 375, height: 667 },  // 일반 모바일
      { width: 768, height: 1024 }, // 태블릿
      { width: 1024, height: 768 }, // 작은 데스크톱
      { width: 1280, height: 720 }, // 일반 데스크톱
    ];
    
    for (const viewport of viewports) {
      await page.setViewportSize(viewport);
      await page.waitForLoadState('networkidle');
      
      // 기본 요소들이 모든 크기에서 작동하는지 확인
      await expect(page.getByRole('heading', { name: '뉴스레터 보관함' })).toBeVisible();
      await expect(page.getByText('전체')).toBeVisible();
      await expect(page.getByRole('searchbox', { name: '검색' })).toBeVisible();
      await expect(page.getByText('최신순')).toBeVisible();
      
      // 검색 기능 테스트
      const searchBox = page.getByRole('searchbox', { name: '검색' });
      await searchBox.fill('테스트');
      await expect(searchBox).toHaveValue('테스트');
      await searchBox.clear();
    }
  });

  test('터치 디바이스에서 탭 기능이 작동해야 한다', async ({ page }) => {
    // 모바일 크기로 설정
    await page.setViewportSize({ width: 375, height: 667 });
    await page.waitForLoadState('networkidle');
    
    // 터치 이벤트로 필터 탭 클릭
    await page.getByText('테크뉴스').tap();
    
    // API 호출 확인
    await page.waitForResponse(response => 
      response.url().includes('/api/v1/articles') && response.status() === 200
    );
    
    // 검색창 터치 테스트
    const searchBox = page.getByRole('searchbox', { name: '검색' });
    await searchBox.tap();
    await searchBox.fill('모바일 테스트');
    await expect(searchBox).toHaveValue('모바일 테스트');
  });
});