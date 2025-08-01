import {
  getNodeByXPath,
  getTextNodesInRange,
  getXPathForNode,
} from './selection';
import { HighlightType } from '../types/highlight';
import { theme } from '@/styles/theme';

export const highlightNodeSegment = (
  node: Text,
  start: number,
  end: number,
  color: string,
  highlightId: string,
) => {
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
};

export const saveSelection = (selection: Selection): HighlightType => {
  const range = selection.getRangeAt(0);
  const container =
    range.commonAncestorContainer.nodeType === Node.TEXT_NODE
      ? range.commonAncestorContainer.parentElement!
      : (range.commonAncestorContainer as Element);

  const xpath = getXPathForNode(container);
  const offsets = getOffsetsFromRangeIncludingMarks(container, range);

  return {
    startXPath: xpath,
    startOffset: offsets.start,
    endXPath: xpath,
    endOffset: offsets.end,
    color: theme.colors.primaryLight,
    id: crypto.randomUUID(),
    text: selection.toString(), // 선택된 텍스트 저장
  };
};

export const getOffsetsFromRangeIncludingMarks = (
  container: Element,
  range: Range,
) => {
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

function createRangeFromOffsetsIncludingMarks(
  container: Node,
  start: number,
  end: number,
) {
  const range = document.createRange();
  const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT);

  let currentOffset = 0;
  let startNode: Text | null = null;
  let endNode: Text | null = null;
  let startNodeOffset = 0;
  let endNodeOffset = 0;

  while (walker.nextNode()) {
    const node = walker.currentNode as Text;
    const len = node.textContent!.length;

    // start node 찾기
    if (!startNode && start >= currentOffset && start <= currentOffset + len) {
      startNode = node;
      startNodeOffset = start - currentOffset;
    }

    // end node 찾기
    if (!endNode && end >= currentOffset && end <= currentOffset + len) {
      endNode = node;
      endNodeOffset = end - currentOffset;
    }

    currentOffset += len;
  }

  if (!startNode || !endNode) throw new Error('Offset 변환 실패');
  range.setStart(startNode, startNodeOffset);
  range.setEnd(endNode, endNodeOffset);

  return range;
}

export const restoreHighlight = (data: HighlightType) => {
  const element = getNodeByXPath(data.startXPath);
  if (!element) return;

  const range = createRangeFromOffsetsIncludingMarks(
    element,
    data.startOffset,
    data.endOffset,
  );

  // === 하이라이트 적용 ===
  const highlightId = data.id || crypto.randomUUID();
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
