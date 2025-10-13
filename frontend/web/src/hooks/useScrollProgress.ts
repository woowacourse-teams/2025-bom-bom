import { useCallback, useEffect, useState } from 'react';

const useScrollProgress = () => {
  const [progressPercentage, setProgressPercentage] = useState(0);

  const calculateProgress = useCallback(() => {
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
    const scrollHeight = document.documentElement.scrollHeight;
    const clientHeight = window.innerHeight;

    const maxScroll = scrollHeight - clientHeight;

    if (maxScroll <= 0) {
      setProgressPercentage(0);
      return;
    }

    const progress = (scrollTop / maxScroll) * 100;
    setProgressPercentage(Math.min(100, Math.max(0, progress)));
  }, []);

  useEffect(() => {
    calculateProgress();

    window.addEventListener('scroll', calculateProgress);

    return () => {
      window.removeEventListener('scroll', calculateProgress);
    };
  }, [calculateProgress]);

  return {
    progressPercentage,
  };
};

export default useScrollProgress;
