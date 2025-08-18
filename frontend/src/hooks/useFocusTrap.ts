import { KeyboardEvent, useCallback, useRef } from 'react';

const useFocusTrap = () => {
  const firstFocusableRef = useRef<HTMLElement | null>(null);
  const lastFocusableRef = useRef<HTMLElement | null>(null);

  const findFocusable = (
    element: HTMLElement,
    focusableList: HTMLElement[] = [],
  ) => {
    if (!element || !element.childNodes) return focusableList;

    for (const child of Array.from(element.children) as HTMLElement[]) {
      if (child.tabIndex >= 0) {
        focusableList.push(child);
      }

      findFocusable(child, focusableList);
    }

    return focusableList;
  };

  const setFocusRef = <T extends HTMLElement>(element: T) => {
    if (!element || element.style.visibility === 'hidden') return;

    const focusableList = findFocusable(element);
    if (focusableList.length === 0) return;

    const [firstFocusable] = focusableList;
    const lastFocusable = focusableList[focusableList.length - 1];

    firstFocusableRef.current = firstFocusable;
    lastFocusableRef.current = lastFocusable;

    firstFocusable.focus();
  };

  const keydownFocusTrapTab = useCallback(
    (event: KeyboardEvent<HTMLElement>) => {
      if (event.key !== 'Tab') {
        return;
      }

      if (
        event.shiftKey &&
        document.activeElement === firstFocusableRef.current
      ) {
        event.preventDefault();
        lastFocusableRef.current?.focus();
      }

      if (document.activeElement === lastFocusableRef.current) {
        event.preventDefault();
        firstFocusableRef.current?.focus();
      }
    },
    [],
  );

  return {
    setFocusRef,
    keydownFocusTrapTab,
  };
};

export default useFocusTrap;
