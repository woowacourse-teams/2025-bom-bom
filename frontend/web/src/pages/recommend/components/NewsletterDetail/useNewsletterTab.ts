import { useNavigate, useSearch } from '@tanstack/react-router';
import { useState, useEffect } from 'react';

export type NewsletterTab = 'detail' | 'previous';

export const useNewsletterTab = () => {
  const { tab: tabParam } = useSearch({ from: '/_bombom/' });
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<NewsletterTab>(
    tabParam ?? 'detail',
  );

  // URL이 바뀌었을 때 상태도 동기화
  useEffect(() => {
    if (tabParam && tabParam !== activeTab) {
      setActiveTab(tabParam as NewsletterTab);
    }
  }, [activeTab, tabParam]);

  const changeTab = (nextTab: NewsletterTab) => {
    setActiveTab(nextTab);
    navigate({
      to: '.',
      search: (prev) => ({ ...prev, tab: nextTab }),
      replace: true,
    });
  };

  return { activeTab, changeTab };
};
