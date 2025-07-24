import { CategoryType } from '@/constants/category';

export type Article = {
  articleId: string;
  title: string;
  contentsSummary: string;
  arrivedDateTime: string;
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

export interface NewsletterResponse {
  newsletterId: number;
  name: string;
  imageUrl: string;
  description: string;
  mainPageUrl: string;
  category: CategoryType;
}

export type NewslettersResponse = NewsletterResponse[];
