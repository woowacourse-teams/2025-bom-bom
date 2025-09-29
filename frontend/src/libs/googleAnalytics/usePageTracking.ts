import { useLocation } from '@tanstack/react-router';
import { useEffect, useRef } from 'react';

export const usePageTracking = () => {
  const location = useLocation();
  const prevPathRef = useRef<string>(null);

  useEffect(() => {
    const currentPath = location.pathname;
    const title = document.title;

    if (typeof window.gtag !== 'function') return;
    window.gtag('event', 'page_view', {
      page_path: currentPath,
      page_title: title,
      previous_path: prevPathRef.current,
    });

    prevPathRef.current = currentPath;
  }, [location.pathname]);
};

export default usePageTracking;
