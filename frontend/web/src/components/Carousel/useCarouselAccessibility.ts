import type { FocusEvent } from 'react';

const useCarouselAccessibility = () => {
  const handleFocus = (e: FocusEvent<HTMLUListElement>) => {
    e.currentTarget.setAttribute('aria-live', 'polite');
  };

  const handleBlur = (e: FocusEvent<HTMLUListElement>) => {
    e.currentTarget.setAttribute('aria-live', 'off');
  };

  return {
    handleFocus,
    handleBlur,
  };
};

export default useCarouselAccessibility;
