export type Article = {
  articleId: string;
  title: string;
  contentsSummary: string;
  arrivedDateTime: Date;
  thumbnailUrl: string;
  expectedReadTime: string;
  isRead: boolean;
  newsletter: {
    name: string;
    imageUrl: string;
    category: string;
  };
};
