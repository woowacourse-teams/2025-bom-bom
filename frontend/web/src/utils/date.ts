export const formatDate = (date: Date, separator: string = '.'): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return year + separator + month + separator + day;
};

export const isToday = (date: Date): boolean => {
  const today = new Date();
  return formatDate(date) === formatDate(today);
};
