import { useCallback, useEffect, useRef } from 'react';

interface UseFocusTrapParams {
  isActive?: boolean;
}

interface FocusElements {
  first: HTMLElement;
  last: HTMLElement;
  current: HTMLElement;
}

interface CycleFocusParams {
  event: KeyboardEvent;
  elements: FocusElements;
}

const FOCUSABLE_ELEMENTS = [
  'input:not([disabled])',
  'select:not([disabled])',
  'textarea:not([disabled])',
  'a[href]',
  'button:not([disabled])',
  '[tabindex]:not([tabindex="-1"]):not(slot)',
  'audio[controls]',
  'video[controls]',
  '[contenteditable]:not([contenteditable="false"])',
  'details > summary:first-of-type',
  'details[open] summary:not(:first-child)',
].join(',');

const isElementVisible = (element: HTMLElement): boolean => {
  const style = window.getComputedStyle(element);
  return (
    style.display !== 'none' &&
    style.visibility !== 'hidden' &&
    style.opacity !== '0' &&
    element.offsetWidth > 0 &&
    element.offsetHeight > 0
  );
};

const isElementNotInert = (element: HTMLElement): boolean => {
  return !element.closest('[inert]');
};

const isFocusable = (element: HTMLElement) => {
  return isElementVisible(element) && isElementNotInert(element);
};

const useFocusTrap = <T extends HTMLElement>({
  isActive = true,
}: UseFocusTrapParams) => {
  const containerRef = useRef<T>(null);
  const previousFocusing = useRef<HTMLElement>(null);

  const getFocusableElements = useCallback(() => {
    const container = containerRef.current;
    if (!container) return [];

    const elements =
      container.querySelectorAll<HTMLElement>(FOCUSABLE_ELEMENTS);
    return Array.from(elements).filter(isFocusable);
  }, []);

  const restoreFocus = useCallback((focusableElements: HTMLElement[]) => {
    const firstElement = focusableElements[0];
    const targetElement = previousFocusing.current || firstElement;
    targetElement?.focus();
  }, []);

  const cycleFocus = useCallback(({ event, elements }: CycleFocusParams) => {
    const { first, last, current } = elements;

    if (current === last && !event.shiftKey) {
      event.preventDefault();
      first.focus();
    } else if (current === first && event.shiftKey) {
      event.preventDefault();
      last.focus();
    }
  }, []);

  const handleKeyDownTab = useCallback(
    (event: KeyboardEvent) => {
      if (event.key !== 'Tab') return;

      const focusableElements = getFocusableElements();
      if (focusableElements.length === 0) return;

      const first = focusableElements[0] as HTMLElement;
      const last = focusableElements[
        focusableElements.length - 1
      ] as HTMLElement;
      const current = document.activeElement as HTMLElement;

      if (!focusableElements.includes(current)) {
        event.preventDefault();
        restoreFocus(focusableElements);
        return;
      }

      cycleFocus({ event, elements: { first, last, current } });
    },
    [getFocusableElements, restoreFocus, cycleFocus],
  );

  const handleTrackFocus = useCallback(() => {
    const focusableElements = getFocusableElements();
    const current = document.activeElement as HTMLElement;
    if (focusableElements.includes(current)) {
      previousFocusing.current = current;
    }
  }, [getFocusableElements]);

  useEffect(() => {
    const focusableElements = getFocusableElements();
    if (focusableElements.length > 0) {
      focusableElements[0]?.focus();
    }
  }, [getFocusableElements]);

  useEffect(() => {
    if (!isActive) return;

    document.addEventListener('keydown', handleKeyDownTab);
    document.addEventListener('focusin', handleTrackFocus);

    return () => {
      document.removeEventListener('keydown', handleKeyDownTab);
      document.removeEventListener('focusin', handleTrackFocus);
    };
  }, [isActive, handleKeyDownTab, handleTrackFocus]);

  return {
    containerRef,
  };
};

export default useFocusTrap;
