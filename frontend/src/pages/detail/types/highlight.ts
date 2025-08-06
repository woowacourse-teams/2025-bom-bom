export interface HighlightType {
  id: number;
  color: string;
  location: {
    startXPath: string;
    startOffset: string;
    endXPath: string;
    endOffset: string;
  };
  articleId: number;
  text: string;
  memo: string;
}
