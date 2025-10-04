import styled from '@emotion/styled';
import { memo, useEffect, useState } from 'react';
import { extractBodyContent, processContent } from './ArticleContent.utils';
import { useHighlightHoverEffect } from '../../hooks/useHighlightHoverEffect';
import { PC_HORIZONTAL_PADDING } from '@/components/PageLayout/PageLayout.constants';
import type { RefObject } from 'react';

const EXTRA_PADDING = 24;

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
  const [scale, setScale] = useState(1);

  useEffect(() => {
    if (!ref.current) return;

    const screenWidth =
      window.outerWidth - (PC_HORIZONTAL_PADDING + EXTRA_PADDING);
    const contentWidth = ref.current?.clientWidth || 1;

    const newScale =
      contentWidth > screenWidth ? screenWidth / contentWidth : 1;

    const newHeight = ref.current.scrollHeight * newScale;
    ref.current.style.height = `${newHeight}px`;
    setScale(newScale);
  }, [ref]);

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
