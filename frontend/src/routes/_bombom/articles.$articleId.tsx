import styled from '@emotion/styled';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createFileRoute, useRouterState } from '@tanstack/react-router';
import { useEffect, useRef, useState } from 'react';
import clockIcon from '../../../public/assets/clock.svg';
import { getArticleById, getArticles, patchArticleRead } from '@/apis/articles';
import Chip from '@/components/Chip/Chip';
import { useThrottle } from '@/hooks/useThrottle';
import EmptyUnreadCard from '@/pages/detail/components/EmptyUnreadCard/EmptyUnreadCard';
import NewsletterItemCard from '@/pages/detail/components/NewsletterItemCard/NewsletterItemCard';
import { formatDate } from '@/utils/date';
import { getScrollPercent } from '@/utils/scroll';

export const Route = createFileRoute('/_bombom/articles/$articleId')({
  component: ArticleDetailPage,
});

/** 특정 노드의 XPath를 구하는 함수 */
function getXPathForElement(node: Node, root: Node = document): string {
  // TextNode일 경우 부모 요소로 이동
  if (node.nodeType === Node.TEXT_NODE) {
    node = node.parentNode!;
  }
  if (node === root) return '.';
  const index =
    Array.from(node.parentNode!.childNodes)
      .filter((n) => n.nodeName === node.nodeName)
      .indexOf(node) + 1;
  return (
    getXPathForElement(node.parentNode!, root) +
    '/' +
    node.nodeName.toLowerCase() +
    `[${index}]`
  );
}

/** XPath로 노드를 다시 찾는 함수 */
function getElementByXPath(
  xpath: string,
  root: Document = document,
): Node | null {
  const result = document.evaluate(
    xpath,
    root,
    null,
    XPathResult.FIRST_ORDERED_NODE_TYPE,
    null,
  );
  return result.singleNodeValue;
}

interface HighlightData {
  startXPath: string;
  startOffset: number;
  endXPath: string;
  endOffset: number;
  color: string;
}

function saveSelection(): HighlightData | null {
  const selection = window.getSelection();
  console.log('selection', selection);
  if (!selection || selection.isCollapsed) return null;

  const range = selection.getRangeAt(0);
  const startXPath = getXPathForElement(range.startContainer);
  const endXPath = getXPathForElement(range.endContainer);

  return {
    startXPath,
    startOffset: range.startOffset,
    endXPath,
    endOffset: range.endOffset,
    color: '#FFEB3B',
  };
}

function restoreHighlight(data: HighlightData) {
  const startElement = getElementByXPath(data.startXPath);
  const endElement = getElementByXPath(data.endXPath);
  if (!startElement || !endElement) return;

  const range = document.createRange();

  const startTextNode = startElement.firstChild!;
  const endTextNode = endElement.firstChild!;
  range.setStart(startTextNode, data.startOffset);
  range.setEnd(endTextNode, data.endOffset);

  // === surroundContents 대신 safe replace ===
  const contents = range.extractContents(); // 선택된 내용을 DOM에서 잘라냄
  const mark = document.createElement('mark');
  mark.style.backgroundColor = data.color;
  mark.appendChild(contents); // 선택 영역을 mark 안에 넣음
  range.insertNode(mark);
}

function ArticleDetailPage() {
  const { articleId } = Route.useParams();
  const loadedAt = useRouterState({
    select: (state) => state.loadedAt,
  });
  const containerRef = useRef<HTMLDivElement>(null);
  const [highlights, setHighlights] = useState<HighlightData[]>([]);
  const queryClient = useQueryClient();

  const { data: currentArticle } = useQuery({
    queryKey: ['article', articleId],
    queryFn: () =>
      getArticleById({
        articleId: Number(articleId),
      }),
  });
  const { data: otherArticles } = useQuery({
    queryKey: ['otherArticles'],
    queryFn: () => getArticles({ date: new Date(), sorted: 'ASC' }),
  });
  const { mutate: updateArticleAsRead } = useMutation({
    mutationKey: ['read', articleId],
    mutationFn: () => patchArticleRead({ articleId: Number(articleId) }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['article', articleId],
      });
    },
  });

  const throttledHandleScroll = useThrottle(() => {
    const scrollPercent = getScrollPercent();
    const elapsedTime = (Date.now() - loadedAt) / 100;

    if (scrollPercent >= 70 && elapsedTime >= 3 && !currentArticle?.isRead) {
      updateArticleAsRead();
    }
  }, 500);

  const handleMouseUp = () => {
    const selectionData = saveSelection();
    if (selectionData) {
      setHighlights((prev) => [...prev, selectionData]);
    }
    window.getSelection()?.removeAllRanges();
  };

  console.log(highlights);

  const applyHighlights = () => {
    highlights.forEach((h) => restoreHighlight(h));
  };

  useEffect(() => {
    window.addEventListener('scroll', throttledHandleScroll);
    return () => window.removeEventListener('scroll', throttledHandleScroll);
  }, [throttledHandleScroll]);

  if (!currentArticle || !otherArticles) return null;

  const unReadArticles = otherArticles?.content.filter(
    (article) => !article.isRead && article.articleId !== Number(articleId),
  );

  return (
    <Container>
      <HeaderWrapper>
        <Title>{currentArticle.title}</Title>
        <MetaInfoRow>
          <Chip text={currentArticle.newsletter.category} />
          <MetaInfoText>from {currentArticle.newsletter.name}</MetaInfoText>
          <MetaInfoText>
            {formatDate(new Date(currentArticle.arrivedDateTime))}
          </MetaInfoText>
          <ReadTimeBox>
            <img src={clockIcon} alt="시계 아이콘" />
            <MetaInfoText>{currentArticle.expectedReadTime}분</MetaInfoText>
          </ReadTimeBox>
        </MetaInfoRow>
      </HeaderWrapper>
      <Divider />
      <button onClick={applyHighlights}>Apply Highlights</button>
      <ContentWrapper
        ref={containerRef}
        onMouseUp={handleMouseUp}
        dangerouslySetInnerHTML={{ __html: currentArticle.contents ?? '' }}
      />
      <Divider />
      <ContentDescription>
        이 뉴스레터가 유용했다면 동료들과 공유해주세요. 피드백이나 제안사항이
        있으시면 언제든 연락 주시기 바랍니다.
      </ContentDescription>
      <TodayArticlesWrapper>
        <TodayArticleTitle>오늘 읽지 않은 다른 아티클</TodayArticleTitle>
        {unReadArticles.length > 0 ? (
          <TodayArticleList>
            {unReadArticles?.map((article) => (
              <NewsletterItemCard key={article.articleId} data={article} />
            ))}
          </TodayArticleList>
        ) : (
          <EmptyUnreadCard />
        )}
      </TodayArticlesWrapper>
    </Container>
  );
}

const Container = styled.div`
  max-width: 700px;
  margin: 0 auto;
  margin-top: 20px;
  padding: 28px;
  border-right: 1px solid ${({ theme }) => theme.colors.stroke};
  border-left: 1px solid ${({ theme }) => theme.colors.stroke};

  display: flex;
  gap: 20px;
  flex-direction: column;
  align-items: center;
`;

const HeaderWrapper = styled.div`
  display: flex;
  gap: 12px;
  flex-direction: column;
  align-items: flex-start;
  align-self: stretch;
`;

const Title = styled.h2`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading1};
`;

const MetaInfoRow = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const MetaInfoText = styled.span`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const ReadTimeBox = styled.div`
  display: flex;
  gap: 4px;
  align-items: center;
`;

const Divider = styled.div`
  width: 100%;
  height: 1px;

  background-color: ${({ theme }) => theme.colors.dividers};
`;

const ContentWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
`;

const ContentDescription = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const TodayArticlesWrapper = styled.div`
  width: 100%;

  display: flex;
  gap: 12px;
  flex-direction: column;
`;

const TodayArticleTitle = styled.h3`
  align-self: flex-start;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading3};
`;

const TodayArticleList = styled.div`
  display: grid;
  gap: 20px;

  grid-template-columns: repeat(2, 1fr);
`;
