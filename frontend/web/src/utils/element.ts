import type { Ref } from 'react';

export const isElementVisible = (element: HTMLElement): boolean => {
  const style = window.getComputedStyle(element);
  return (
    style.display !== 'none' &&
    style.visibility !== 'hidden' &&
    style.opacity !== '0' &&
    element.offsetWidth > 0 &&
    element.offsetHeight > 0
  );
};

export const compoundRefs = <T>(...refs: Ref<T>[]) => {
  return (node: T) => {
    refs.forEach((ref) => {
      if (!ref) return;

      if (typeof ref === 'function') {
        ref(node);
        return;
      }

      ref.current = node;
    });
  };
};

export const extractBodyContent = (content: string) => {
  const parser = new DOMParser();
  const doc = parser.parseFromString(content, 'text/html');
  const bodyContent = doc.body.innerHTML;

  return bodyContent;
};

export function cutHtmlByTextRatio(html?: string, ratio = 1) {
  if (!html) return '';

  const parser = new DOMParser();
  const doc = parser.parseFromString(html, 'text/html');

  const fullText = doc.body.innerText;
  const targetLength = Math.floor(fullText.length * ratio);

  let current = 0;
  const walker = doc.createTreeWalker(doc.body, NodeFilter.SHOW_TEXT);
  let node: Node | null;

  while ((node = walker.nextNode())) {
    const textNode = node as Text;
    const nodeLength = textNode.textContent?.length ?? 0;

    // 현재 노드 추가 후 targetLength를 초과하는지 확인
    if (current + nodeLength >= targetLength) {
      // 현재 노드의 다음 형제들 삭제
      let sibling = textNode.nextSibling;
      while (sibling) {
        const nextSibling = sibling.nextSibling;
        sibling.remove();
        sibling = nextSibling;
      }

      // 부모 요소의 다음 형제들도 모두 삭제
      let parent = textNode.parentNode;
      while (parent && parent !== doc.body) {
        let parentSibling = parent.nextSibling;
        while (parentSibling) {
          const nextSibling = parentSibling.nextSibling;
          parentSibling.remove();
          parentSibling = nextSibling;
        }
        parent = parent.parentNode;
      }

      break;
    }

    current += nodeLength;
  }

  return doc.body.innerHTML;
}
