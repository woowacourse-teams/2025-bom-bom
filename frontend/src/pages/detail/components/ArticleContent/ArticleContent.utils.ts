export const processContent = (content: string): string => {
  if (!content) return '';

  return content
    .replace(/\\r\\n/g, '<br>') // Windows 개행
    .replace(/\r/g, '<br>') // Mac 개행 (구버전)
    .replace(/\n/g, '<br>'); // Unix/Linux 개행
};
