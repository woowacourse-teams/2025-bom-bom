export const getScrollPercent = () => {
  const { scrollTop, scrollHeight, clientHeight } = document.documentElement;

  if (scrollHeight <= clientHeight) return 100;

  return (scrollTop / (scrollHeight - clientHeight)) * 100;
};
