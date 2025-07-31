import styled from '@emotion/styled';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createFileRoute, useRouterState } from '@tanstack/react-router';
import { useEffect, useState } from 'react';
import clockIcon from '../../../public/assets/clock.svg';
import { getArticleById, getArticles, patchArticleRead } from '@/apis/articles';
import Chip from '@/components/Chip/Chip';
import { useThrottle } from '@/hooks/useThrottle';
import EmptyUnreadCard from '@/pages/detail/components/EmptyUnreadCard/EmptyUnreadCard';
import FloatingToolbar from '@/pages/detail/components/FloatingToolbar/FloatingToolbar';
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
      .indexOf(node as ChildNode) + 1;
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

function getTextNodesInRange(range: Range): Text[] {
  const textNodes: Text[] = [];

  // === 선택된 범위가 단일 TextNode 내부 ===
  if (
    range.startContainer === range.endContainer &&
    range.startContainer.nodeType === Node.TEXT_NODE
  ) {
    textNodes.push(range.startContainer as Text);
    return textNodes;
  }

  // === 여러 노드일 경우 기존 TreeWalker 사용 ===
  const walker = document.createTreeWalker(
    range.commonAncestorContainer,
    NodeFilter.SHOW_TEXT,
    {
      acceptNode(node) {
        return range.intersectsNode(node)
          ? NodeFilter.FILTER_ACCEPT
          : NodeFilter.FILTER_REJECT;
      },
    },
  );

  let node: Node | null;
  while ((node = walker.nextNode())) {
    textNodes.push(node as Text);
  }
  return textNodes;
}

interface HighlightData {
  startXPath: string;
  startOffset: number;
  endXPath: string;
  endOffset: number;
  color: string;
}

function saveSelection(selection: Selection): HighlightData {
  const range = selection.getRangeAt(0);
  const startXPath = getXPathForElement(range.startContainer);
  const endXPath = getXPathForElement(range.endContainer);

  return {
    color: '#FFEB3B',
    startXPath,
    startOffset: range.startOffset,
    endXPath,
    endOffset: range.endOffset,
  };
}

function highlightNodeSegment(
  node: Text,
  start: number,
  end: number,
  color: string,
  highlightId: string,
) {
  const parent = node.parentNode!;
  const before = node.textContent!.slice(0, start);
  const middle = node.textContent!.slice(start, end);
  const after = node.textContent!.slice(end);

  const mark = document.createElement('mark');
  mark.style.backgroundColor = color;
  mark.dataset.highlightId = highlightId;
  mark.textContent = middle;

  const frag = document.createDocumentFragment();
  if (before) frag.appendChild(document.createTextNode(before));
  frag.appendChild(mark);
  if (after) frag.appendChild(document.createTextNode(after));

  parent.replaceChild(frag, node);
}

function isAlreadyHighlighted(node: Text) {
  return node.parentNode?.nodeName === 'MARK';
}

export function restoreHighlight(data: HighlightData) {
  const startElement = getElementByXPath(data.startXPath);
  const endElement = getElementByXPath(data.endXPath);
  if (!startElement || !endElement) return;

  const range = document.createRange();
  const startTextNode = startElement.firstChild as Text;
  const endTextNode = endElement.firstChild as Text;
  range.setStart(startTextNode, data.startOffset);
  range.setEnd(endTextNode, data.endOffset);

  const textNodes = getTextNodesInRange(range);

  // === 그룹 ID 생성 ===
  const highlightId = crypto.randomUUID();

  // === 단일 노드 ===
  if (textNodes.length === 1) {
    const node = textNodes[0];
    if (!isAlreadyHighlighted(node) && data.startOffset < data.endOffset) {
      highlightNodeSegment(
        node,
        data.startOffset,
        data.endOffset,
        data.color,
        highlightId,
      );
    }
    return;
  }

  // === 여러 노드 ===
  textNodes.forEach((node, index) => {
    if (isAlreadyHighlighted(node)) return;

    const isFirst = index === 0;
    const isLast = index === textNodes.length - 1;

    let start = 0;
    let end = node.textContent!.length;
    if (isFirst) start = data.startOffset;
    if (isLast) end = data.endOffset;

    if (start < end)
      highlightNodeSegment(node, start, end, data.color, highlightId);
  });
}

function ArticleDetailPage() {
  const { articleId } = Route.useParams();
  const loadedAt = useRouterState({
    select: (state) => state.loadedAt,
  });
  // const containerRef = useRef<HTMLDivElement>(null);
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

  useEffect(() => {
    window.addEventListener('scroll', throttledHandleScroll);
    return () => window.removeEventListener('scroll', throttledHandleScroll);
  }, [throttledHandleScroll]);

  useEffect(() => {
    highlights.forEach((h) => restoreHighlight(h));
  }, [highlights]);

  useEffect(() => {
    document.addEventListener('mouseover', (e) => {
      const target = e.target as HTMLElement;
      if (target.tagName === 'MARK' && target.dataset.highlightId) {
        const id = target.dataset.highlightId;
        document
          .querySelectorAll(`mark[data-highlight-id="${id}"]`)
          .forEach((el) => {
            el.classList.add('hovered-highlight');
          });
      }
    });

    document.addEventListener('mouseout', (e) => {
      const target = e.target as HTMLElement;
      if (target.tagName === 'MARK' && target.dataset.highlightId) {
        const id = target.dataset.highlightId;
        document
          .querySelectorAll(`mark[data-highlight-id="${id}"]`)
          .forEach((el) => {
            el.classList.remove('hovered-highlight');
          });
      }
    });

    // Click 시 Floating Toolbar 열기
    document.addEventListener('click', (e) => {
      const target = e.target as HTMLElement;
      if (target.tagName === 'MARK' && target.dataset.highlightId) {
        // openFloatingToolbar(target); // FloatingToolbar 열기 로직 호출
        console.log('CcCCCCCCClick');
      }
    });
  }, []);

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
      <ContentWrapper
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
      <FloatingToolbar
        onSave={(selection) => {
          console.log(selection);
          const highLightData = saveSelection(selection);
          // console.log(highLightData);
          setHighlights((prev) => [...prev, highLightData]);
        }}
      />
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
