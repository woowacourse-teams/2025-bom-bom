import styled from '@emotion/styled';
import { forwardRef, memo } from 'react';
import { processContent } from './ArticleContent.utils';

interface ArticleContentProps {
  content?: string;
}

const ArticleContent = forwardRef<HTMLDivElement, ArticleContentProps>(
  ({ content }, ref) => {
    return (
      <Container
        ref={ref}
        dangerouslySetInnerHTML={{
          __html: processContent(content ?? ''),
        }}
      />
    );
  },
);

export default memo(ArticleContent);

const Container = styled.div`
  overflow: visible;

  display: flex;
  flex-direction: column;
  align-items: flex-start;

  white-space: normal;

  word-break: break-all;
  word-wrap: break-word;

  /* 모바일 텍스트 선택 최적화 */
  -webkit-user-select: text;
  -moz-user-select: text;
  -ms-user-select: text;
  user-select: text;
  
  /* iOS Safari 최적화 */
  -webkit-touch-callout: default;
  -webkit-tap-highlight-color: transparent;

  mark[data-highlight-id] {
    background-color: #ffeb3b;
    transition: box-shadow 0.2s ease-in-out;
    /* 모바일에서 하이라이트된 텍스트 터치 최적화 */
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
  }

  mark[data-highlight-id].hovered-highlight {
    box-shadow: 0 0 6px rgb(0 0 0 / 30%);
    cursor: pointer;
  }
`;
