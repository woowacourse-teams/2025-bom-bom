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

const useFocusTrap = <T extends HTMLElement>({
  isActive,
}: UseFocusTrapParams) => {
  const containerRef = useRef<T>(null);

  const isElementVisible = useCallback((element: HTMLElement): boolean => {
    const style = window.getComputedStyle(element);
    return (
      style.display !== 'none' &&
      style.visibility !== 'hidden' &&
      style.opacity !== '0' &&
      element.offsetWidth > 0 &&
      element.offsetHeight > 0
    );
  }, []);

  const isElementNotInert = useCallback((element: HTMLElement): boolean => {
    return !element.closest('[inert]');
  }, []);

  const getFocusableElements = useCallback((): HTMLElement[] => {
    if (!containerRef.current) return [];

    const elements = containerRef.current.querySelectorAll(FOCUSABLE_ELEMENTS);
    return Array.from(elements).filter((element) => {
      return (
        element instanceof HTMLElement &&
        isElementVisible(element) &&
        isElementNotInert(element)
      );
    }) as HTMLElement[];
  }, [isElementNotInert, isElementVisible]);

  const focusFirstElement = useCallback(() => {
    const focusableElements = getFocusableElements();
    if (focusableElements.length > 0) {
      focusableElements[0]?.focus();
    }
  }, [getFocusableElements]);

  const keydownShift = useCallback(
    (event: KeyboardEvent) => {
      const focusableElements = getFocusableElements();
      if (focusableElements.length === 0) return;

      const firstElement = focusableElements[0];
      const lastElement = focusableElements[focusableElements.length - 1];

      if (document.activeElement === lastElement && !event.shiftKey) {
        event.preventDefault();
        firstElement?.focus();
      } else if (document.activeElement === firstElement && event.shiftKey) {
        event.preventDefault();
        lastElement?.focus();
      }
    },
    [getFocusableElements],
  );

  const keydownTab = useCallback(
    (event: KeyboardEvent) => {
      if (!isActive || !containerRef.current) return;

      if (event.key === 'Tab') {
        keydownShift(event);
      }
    },
    [isActive, keydownShift],
  );

  const handleClick = useCallback(
    (event: Event) => {
      if (!isActive || !containerRef.current) return;

      if (!containerRef.current.contains(event.target as Node)) {
        event.preventDefault();
        focusFirstElement();
      }
    },
    [isActive, focusFirstElement],
  );

  useEffect(() => {
    if (!isActive) return;

    focusFirstElement();

    document.addEventListener('keydown', keydownTab);
    document.addEventListener('click', handleClick);

    return () => {
      document.removeEventListener('keydown', keydownTab);
      document.removeEventListener('click', handleClick);
    };
  }, [isActive, focusFirstElement, keydownTab, handleClick]);

  return {
    containerRef,
  };
};

export default useFocusTrap;
