import {
  getNodeByXPath,
  getTextNodesInRange,
  getXPathForNode,
} from './selection';
import { HighlightType } from '../types/highlight';
import { PostHighlightParams } from '@/apis/highlight';
import { theme } from '@/styles/theme';

export const highlightNodeSegment = (
  node: Text,
  start: number,
  end: number,
  color: string,
  highlightId: number,
) => {
  const parent = node.parentNode!;
  const before = node.textContent!.slice(0, start);
  const middle = node.textContent!.slice(start, end);
  const after = node.textContent!.slice(end);

  const mark = document.createElement('mark');
  mark.style.backgroundColor = color;
  mark.dataset.highlightId = highlightId.toString();
  mark.textContent = middle;

  const frag = document.createDocumentFragment();
  if (before) frag.appendChild(document.createTextNode(before));
  frag.appendChild(mark);
  if (after) frag.appendChild(document.createTextNode(after));

  parent.replaceChild(frag, node);
};

export const saveSelection = (
  selection: Selection,
  articleId: number,
): PostHighlightParams => {
  const range = selection.getRangeAt(0);
  const container =
    range.commonAncestorContainer.nodeType === Node.TEXT_NODE
      ? range.commonAncestorContainer.parentElement!
      : (range.commonAncestorContainer as Element);

  const xpath = getXPathForNode(container);
  const offsets = getHighlightOffsets(container, range);

  return {
    highlight: {
      location: {
        startXPath: xpath,
        startOffset: offsets.start,
        endXPath: xpath,
        endOffset: offsets.end,
      },
      articleId,
      color: theme.colors.primaryLight,
      text: selection.toString(), // 선택된 텍스트 저장
    },
  };
};

export const getHighlightOffsets = (container: Element, range: Range) => {
  let start = -1;
  let end = -1;
  let currentOffset = 0;

  const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT);

  while (walker.nextNode()) {
    const node = walker.currentNode as Text;
    if (node === range.startContainer)
      start = currentOffset + range.startOffset;
    if (node === range.endContainer) end = currentOffset + range.endOffset;
    currentOffset += node.textContent!.length;
  }
  return { start, end };
};

function getHighlightRange(container: Node, start: number, end: number) {
  const range = document.createRange();
  const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT);

  let currentOffset = 0;

  const positions = {
    startNode: null as Text | null,
    endNode: null as Text | null,
    startOffset: 0,
    endOffset: 0,
  };

  while (walker.nextNode()) {
    const node = walker.currentNode as Text;
    const len = node.textContent!.length;

    // start node 찾기
    if (
      !positions.startNode &&
      start >= currentOffset &&
      start <= currentOffset + len
    ) {
      positions.startNode = node;
      positions.startOffset = start - currentOffset;
    }

    // end node 찾기
    if (
      !positions.endNode &&
      end >= currentOffset &&
      end <= currentOffset + len
    ) {
      positions.endNode = node;
      positions.endOffset = end - currentOffset;
    }

    currentOffset += len;
  }

  if (!positions.startNode || !positions.endNode) {
    throw new Error('Offset 변환 실패');
  }

  range.setStart(positions.startNode, positions.startOffset);
  range.setEnd(positions.endNode, positions.endOffset);

  return range;
}

export const restoreHighlight = (data: HighlightType) => {
  const element = getNodeByXPath(data.location.startXPath);
  if (!element) return;

  const range = getHighlightRange(
    element,
    Number(data.location.startOffset),
    Number(data.location.endOffset),
  );

  // === 하이라이트 적용 ===
  const highlightId = data.id || Math.random();
  const textNodes = getTextNodesInRange(range); // mark 제외 처리 가능
  textNodes.forEach((node, index) => {
    const isFirst = index === 0;
    const isLast = index === textNodes.length - 1;

    let start = 0;
    let end = node.textContent!.length;
    if (isFirst) start = range.startOffset;
    if (isLast) end = range.endOffset;

    if (start < end) {
      highlightNodeSegment(node, start, end, data.color, highlightId);
    }
  });
};
