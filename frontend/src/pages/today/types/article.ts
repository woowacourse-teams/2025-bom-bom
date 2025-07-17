export type Article = {
  articleId: string;
  title: string;
  contentsSummary: string;
  arrivedDateTime: Date;
  thumbnailUrl: string;
  expectedReadTime: number;
  isRead: boolean;
  newsletter: Newsletter;
};

export type Newsletter = {
  name: string;
  imageUrl: string;
  category: string;
};
