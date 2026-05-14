import { components } from '@/types/openapi';

interface ArticleReadStats {
  total: number;
  read: number;
  unread: number;
}

export function getArticleReadStats(
  articles: components['schemas']['ArticleResponse'][],
): ArticleReadStats {
  return articles.reduce<ArticleReadStats>(
    (acc, article) => {
      acc.total++;
      if (article.isRead) acc.read++;
      else acc.unread++;
      return acc;
    },
    { total: 0, read: 0, unread: 0 },
  );
}
