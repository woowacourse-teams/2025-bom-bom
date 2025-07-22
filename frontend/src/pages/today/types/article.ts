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

export interface ArticleDetail {
  title: string;
  contents: string;
  arrivedDateTime: string;
  expectedReadTime: number;
  newsletter: {
    name: string;
    email: string;
    imageUrl: string;
    category: string;
  };
}

export type Newsletter = {
  name: string;
  imageUrl: string;
  category: string;
};

export interface NewsletterResponse {
  newsletterId: number;
  name: string;
  imageUrl: string;
  description: string;
  mainPageUrl: string;
}

export type NewslettersResponse = NewsletterResponse[];
