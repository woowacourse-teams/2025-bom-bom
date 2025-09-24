import { useLocation } from '@tanstack/react-router';
import { useEffect, useRef } from 'react';

const TITLE_MAP: Record<string, string> = {
  '/': '오늘의 뉴스레터',
  '/storage': '뉴스레터 보관함',
  '/recommend': '뉴스레터 추천',
  '/login': '로그인',
  '/signup': '회원가입',
  '/guide': '가이드 보관함',
  '/memo': '메모 보관함',
  '/bookmark': '북마크 보관함',
};

const PageTitle = () => {
  const location = useLocation();
  const prevPathRef = useRef<string>(null);

  useEffect(() => {
    if (typeof window.gtag !== 'function') return;
    const currentPath = location.pathname;

    let title = '내 서비스';
    if (currentPath.startsWith('/articles/')) {
      if (currentPath.includes('/guide')) {
        title = '가이드 메일 상세';
      } else {
        title = '아티클 상세';
      }
    } else {
      title = TITLE_MAP[currentPath] ?? '내 서비스';
    }
    document.title = title;

    window.gtag('event', 'page_view', {
      page_path: currentPath,
      page_title: title,
      previous_path: prevPathRef.current,
    });
    prevPathRef.current = currentPath;
  }, [location.pathname]);

  return null;
};

export default PageTitle;
