import { KeyboardEvent, RefObject, useCallback } from 'react';

const useFocusTrap = (containerRef: RefObject<HTMLElement | null>) => {
  const findFocusable = useCallback(() => {
    const container = containerRef.current;
    if (!container) return [];

    const walker = document.createTreeWalker(
      container,
      NodeFilter.SHOW_ELEMENT,
      {
        acceptNode(node) {
          const element = node as HTMLElement;
          if (
            element.tabIndex >= 0 &&
            !element.hasAttribute('disabled') &&
            element.offsetParent !== null
          ) {
            return NodeFilter.FILTER_ACCEPT;
          }
          return NodeFilter.FILTER_SKIP;
        },
      },
    );

    const focusableElements: HTMLElement[] = [];
    while (walker.nextNode()) {
      focusableElements.push(walker.currentNode as HTMLElement);
    }

    return focusableElements;
  }, [containerRef]);

  const initFocus = useCallback(() => {
    const [first] = findFocusable();
    first?.focus();
  }, [findFocusable]);

  const keydownFocusTrapTab = useCallback(
    (event: KeyboardEvent<HTMLElement>) => {
      if (event.key !== 'Tab') return;

      const focusableElements = findFocusable();
      if (focusableElements.length === 0) return;

      const firstFocusable = focusableElements[0];
      const lastFocusable = focusableElements[focusableElements.length - 1];

      if (event.shiftKey && document.activeElement === firstFocusable) {
        event.preventDefault();
        lastFocusable?.focus();
      }

      if (!event.shiftKey && document.activeElement === lastFocusable) {
        event.preventDefault();
        firstFocusable?.focus();
      }
    },
    [findFocusable],
  );

  return {
    initFocus,
    keydownFocusTrapTab,
  };
};

export default useFocusTrap;
