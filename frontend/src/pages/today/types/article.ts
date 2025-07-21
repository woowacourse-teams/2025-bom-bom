export type Article = {
  articleId: string;
  title: string;
  contentsSummary: string;
  arrivedDateTime: Date;
  thumbnailUrl?: string;
  expectedReadTime: number;
  isRead: boolean;
  newsletter: Newsletter;
};

export type Newsletter = {
  name: string;
  imageUrl: string;
  category: string;
};

// API 명세에 맞는 ArticleResponse 타입
export interface ArticleResponse {
  title: string;
  contents: string;
  arrivedDateTime: string;
  expectedReadTime: number;
  newsLetter: {
    name: string;
    email: string;
    imageUrl: string;
    category: string;
  };
}

// NewsletterResponse 및 배열 타입
export interface NewsletterResponse {
  newsletterId: number;
  name: string;
  imageUrl: string;
  description: string;
  mainPageUrl: string;
}

export type NewslettersResponse = NewsletterResponse[];
