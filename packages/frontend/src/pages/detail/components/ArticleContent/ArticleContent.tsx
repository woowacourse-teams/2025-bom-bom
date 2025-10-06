import styled from '@emotion/styled';
import { memo } from 'react';
import { extractBodyContent, processContent } from './ArticleContent.utils';
import { useAutoScaleContent } from '../../hooks/useAutoScaleContent';
import { useHighlightHoverEffect } from '../../hooks/useHighlightHoverEffect';
import type { RefObject } from 'react';

interface ArticleContentProps {
  ref: RefObject<HTMLDivElement | null>;
  newsletterName: string;
  content?: string;
}

const ArticleContent = ({
  ref,
  newsletterName,
  content,
}: ArticleContentProps) => {
  const bodyContent = extractBodyContent(content ?? '');
  const scale = useAutoScaleContent(ref);

  useHighlightHoverEffect();

  return (
    <Container
      ref={ref}
      scale={scale}
      dangerouslySetInnerHTML={{
        __html: processContent(newsletterName, bodyContent),
      }}
    />
  );
};

export default memo(ArticleContent);

const Container = styled.div<{ scale: number }>`
  overflow: visible;

  display: flex;
  flex-direction: column;
  align-items: flex-start;

  white-space: pre-line;

  -webkit-tap-highlight-color: rgb(0 0 0 / 10%);
  -webkit-touch-callout: default;

  transform: ${({ scale }) => `scale(${scale})`};
  transform-origin: top;
  user-select: text;

  word-break: break-all;
  word-wrap: break-word;

  a {
    color: ${({ theme }) => theme.colors.info};

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
