export interface HighlightType {
  id: number;
  color: string;
  location: {
    startXPath: string;
    startOffset: number;
    endXPath: string;
    endOffset: number;
  };
  articleId: number;
  text: string;
  memo: string;
  newsletterName: string;
  newsletterImageUrl: string;
  articleTitle: string;
  createdAt: string;
}
