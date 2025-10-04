import { useEffect, useState } from 'react';
import { PC_HORIZONTAL_PADDING } from '@/components/PageLayout/PageLayout.constants';
import type { RefObject } from 'react';

export const useAutoScaleContent = (ref: RefObject<HTMLDivElement | null>) => {
  const [scale, setScale] = useState(1);

  useEffect(() => {
    if (!ref.current) return;

    const screenWidth = window.outerWidth - PC_HORIZONTAL_PADDING;
    const contentWidth = ref.current?.clientWidth || 1;

    const newScale =
      contentWidth > screenWidth ? screenWidth / contentWidth : 1;

    if (newScale === 1) return;

    const newHeight = ref.current.scrollHeight * newScale;
    ref.current.style.height = `${newHeight}px`;
    setScale(newScale);
  }, [ref]);

  return scale;
};
