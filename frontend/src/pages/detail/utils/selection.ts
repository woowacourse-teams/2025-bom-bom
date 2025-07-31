/** 특정 노드의 XPath를 구하는 함수 */
export const getXPathForElement = (
  node: Node,
  root: Node = document,
): string => {
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
};

/** XPath로 노드를 다시 찾는 함수 */
export const getElementByXPath = (xpath: string, root: Document = document) => {
  const result = document.evaluate(
    xpath,
    root,
    null,
    XPathResult.FIRST_ORDERED_NODE_TYPE,
    null,
  );
  return result.singleNodeValue;
};

export const getTextNodesInRange = (range: Range): Text[] => {
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
};
