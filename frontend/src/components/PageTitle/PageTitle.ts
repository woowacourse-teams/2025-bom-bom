import { useLocation } from '@tanstack/react-router';
import { useEffect } from 'react';

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

  useEffect(() => {
    let title = '내 서비스';

    if (location.pathname.startsWith('/articles/')) {
      if (location.pathname.includes('/guide')) {
        title = '가이드 메일 상세';
      } else {
        title = '아티클 상세';
      }
    } else {
      title = TITLE_MAP[location.pathname] ?? '내 서비스';
    }
    document.title = title;
  }, [location.pathname]);

  return null;
};

export default PageTitle;
