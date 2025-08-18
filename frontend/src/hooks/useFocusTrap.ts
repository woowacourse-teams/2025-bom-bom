import { KeyboardEvent, RefObject, useCallback, useRef } from 'react';

const useFocusTrap = (containerRef: RefObject<HTMLElement | null>) => {
  const firstFocusableRef = useRef<HTMLElement | null>(null);
  const lastFocusableRef = useRef<HTMLElement | null>(null);

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
    const focusableElements = findFocusable();
    console.log(focusableElements);
    const firstElement = focusableElements[0];
    const lastElement = focusableElements[focusableElements.length - 1];
    if (!firstElement || !lastElement) return;

    firstFocusableRef.current = firstElement;
    lastFocusableRef.current = lastElement;

    firstFocusableRef.current.focus();
  }, [findFocusable]);

  const keydownFocusTrapTab = useCallback(
    (event: KeyboardEvent<HTMLElement>) => {
      const isTabKeyDown = !event.shiftKey && event.key === 'Tab';
      const isShiftTabKeyDown = event.shiftKey && event.key === 'Tab';
      if (!isTabKeyDown && !isShiftTabKeyDown) return;

      if (
        isShiftTabKeyDown &&
        document.activeElement === firstFocusableRef.current
      ) {
        event.preventDefault();
        lastFocusableRef.current?.focus();
      } else if (
        isTabKeyDown &&
        document.activeElement === lastFocusableRef.current
      ) {
        event.preventDefault();
        firstFocusableRef.current?.focus();
      }
    },
    [],
  );

  return {
    initFocus,
    keydownFocusTrapTab,
  };
};

export default useFocusTrap;
