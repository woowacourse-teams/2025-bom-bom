import styled from '@emotion/styled';
import { memo } from 'react';
import GuideMail1 from './GuideMail1';
import GuideMail2 from './GuideMail2';
import GuideMail3 from './GuideMail3';
import { useHighlightHoverEffect } from '@/pages/detail/hooks/useHighlightHoverEffect';
import type { RefObject } from 'react';

interface ArticleContentProps {
  ref: RefObject<HTMLDivElement | null>;
  articleId: number;
}

const ArticleContent = ({ ref, articleId }: ArticleContentProps) => {
  useHighlightHoverEffect();

  return (
    <Container ref={ref}>
      {articleId === 1 ? (
        <GuideMail1 />
      ) : articleId === 2 ? (
        <GuideMail2 />
      ) : (
        <GuideMail3 />
      )}
    </Container>
  );
};

export default memo(ArticleContent);

const Container = styled.div`
  overflow: visible;

  display: flex;
  flex-direction: column;
  align-items: flex-start;

  white-space: pre-line;

  -webkit-tap-highlight-color: rgb(0 0 0 / 10%);
  -webkit-touch-callout: default;
  user-select: text;

  word-break: break-all;
  word-wrap: break-word;

  mark[data-highlight-id] {
    background-color: #ffeb3b;
    transition: box-shadow 0.2s ease-in-out;
  }

  mark[data-highlight-id].hovered-highlight {
    box-shadow: 0 0 6px rgb(0 0 0 / 30%);
    cursor: pointer;
  }
`;
