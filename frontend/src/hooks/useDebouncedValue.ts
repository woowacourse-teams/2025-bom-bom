import { useEffect, useState } from 'react';

export const useDebouncedValue = <T>(value: T, wait: number): T => {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timerId = setTimeout(() => setDebounced(value), wait);
    return () => clearTimeout(timerId);
  }, [value, wait]);

  return debounced;
};
