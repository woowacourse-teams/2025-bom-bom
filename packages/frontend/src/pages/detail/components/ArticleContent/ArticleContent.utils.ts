export const processContent = (category: string, content: string): string => {
  if (!content) return '';

  if (category === '긱뉴스') {
    return (
      content
        .replace(/\\r\\n/g, '<br>') // Windows 개행
        .replace(/\r/g, '<br>') // Mac 개행 (구버전)
        .replace(/\n/g, '<br>') // Unix/Linux 개행
        // 긱뉴스 절취선을 제거하기 위해서 추가
        // * 링크 패턴을 <ul><li><a>로 변환
        .replace(
          /-{5,}\s*([\s\S]*?)\s*\*\s*(https?:\/\/\S+)\s*-{5,}/g,
          '<ul><li><a href="$2" target="_blank" rel="noopener noreferrer">$1</a></li></ul>',
        )
        .trim()
    );
  }

  return content
    .replace(/\\r\\n/g, '') // Windows 개행
    .replace(/\r/g, '') // Mac 개행 (구버전)
    .replace(/\n/g, '') // Unix/Linux 개행
    .trim();
};

export const extractBodyContent = (content: string) => {
  const parser = new DOMParser();
  const doc = parser.parseFromString(content, 'text/html');
  const bodyContent = doc.body.innerHTML;

  return bodyContent;
};
