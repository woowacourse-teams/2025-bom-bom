import { useLocation } from '@tanstack/react-router';
import { useEffect } from 'react';

export const usePageTracking = () => {
  const location = useLocation();

  useEffect(() => {
    if (typeof window.gtag !== 'function') {
      console.warn('[GA] gtag is not initialized');
      return;
    }

    window.gtag('event', 'page_view', {
      page_path: location.pathname,
      page_title: document.title,
    });
  }, [location.pathname]);
};
