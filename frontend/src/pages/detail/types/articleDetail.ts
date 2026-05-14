export type ArticleDetail = {
  title: string;
  contents: string;
  isRead: boolean;
  arrivedDateTime: string;
  expectedReadTime: number;
  newsletter: {
    name: string;
    email: string;
    imageUrl: string;
    category: string;
  };
};
