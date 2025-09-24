import styled from '@emotion/styled';
import { memo } from 'react';
import { extractBodyContent, processContent } from './ArticleContent.utils';
import { useHighlightHoverEffect } from '../../hooks/useHighlightHoverEffect';
import type { RefObject } from 'react';

interface ArticleContentProps {
  ref: RefObject<HTMLDivElement | null>;
  content?: string;
}

const ArticleContent = ({ ref, content }: ArticleContentProps) => {
  const bodyContent = extractBodyContent(content ?? '');

  useHighlightHoverEffect();

  return (
    <Container
      ref={ref}
      dangerouslySetInnerHTML={{
        __html: processContent(bodyContent),
      }}
    />
  );
};

export default memo(ArticleContent);

const Container = styled.div`
  overflow: visible;

  display: flex;
  flex-direction: column;
  align-items: flex-start;

  white-space: pre-line;

  word-break: break-all;
  word-wrap: break-word;

  a {
    color: #1a73e8;

    cursor: pointer;
    text-decoration: underline;

    &:hover {
      text-decoration: none;
    }
  }

  mark[data-highlight-id] {
    background-color: #ffeb3b;
    transition: box-shadow 0.2s ease-in-out;
  }

  mark[data-highlight-id].hovered-highlight {
    box-shadow: 0 0 6px rgb(0 0 0 / 30%);
    cursor: pointer;
  }
`;
