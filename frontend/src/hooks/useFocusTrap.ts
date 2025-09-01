import { useCallback, useEffect, useRef } from 'react';

interface UseFocusTrapParams {
  isActive?: boolean;
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
  isActive,
}: UseFocusTrapParams) => {
  const containerRef = useRef<T>(null);
  const previousFocusing = useRef<HTMLElement>(null);

  const getFocusableElements = useCallback((): HTMLElement[] => {
    const container = containerRef.current;
    if (!container) return [];

    const elements =
      container.querySelectorAll<HTMLElement>(FOCUSABLE_ELEMENTS);
    return Array.from(elements).filter(isFocusable);
  }, []);

  const keydownShift = useCallback(
    (event: KeyboardEvent) => {
      const focusableElements = getFocusableElements();
      if (focusableElements.length === 0) return;

      const firstElement = focusableElements[0];
      const lastElement = focusableElements[focusableElements.length - 1];
      const activeElement = document.activeElement as HTMLElement;

      if (!focusableElements.includes(activeElement)) {
        event.preventDefault();
        const targetElement = previousFocusing.current || firstElement;
        targetElement?.focus();
        return;
      }

      if (activeElement === lastElement && !event.shiftKey) {
        event.preventDefault();
        firstElement?.focus();
      } else if (activeElement === firstElement && event.shiftKey) {
        event.preventDefault();
        lastElement?.focus();
      }
    },
    [getFocusableElements],
  );

  const keydownTab = useCallback(
    (event: KeyboardEvent) => {
      if (event.key === 'Tab') {
        keydownShift(event);
      }
    },
    [keydownShift],
  );

  const trackFocus = useCallback(() => {
    const focusableElements = getFocusableElements();
    const activeElement = document.activeElement as HTMLElement;
    if (focusableElements.includes(activeElement)) {
      previousFocusing.current = activeElement;
    }
  }, [getFocusableElements]);

  const focusFirstElement = useCallback(() => {
    const focusableElements = getFocusableElements();
    if (focusableElements.length > 0) {
      focusableElements[0]?.focus();
    }
  }, [getFocusableElements]);

  useEffect(() => {
    if (!isActive) return;

    focusFirstElement();

    document.addEventListener('keydown', keydownTab);
    document.addEventListener('focusin', trackFocus);

    return () => {
      document.removeEventListener('keydown', keydownTab);
      document.removeEventListener('focusin', trackFocus);
    };
  }, [isActive, focusFirstElement, keydownTab, trackFocus]);

  return {
    containerRef,
  };
};

export default useFocusTrap;
