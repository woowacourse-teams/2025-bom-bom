import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { getArticleById } from '../pages/today/apis/articles';
import PageLayout from '../components/PageLayout/PageLayout';

export const Route = createFileRoute('/articles/$articleId')({
  component: ArticleDetailPage,
});

function ArticleDetailPage() {
  const { articleId } = Route.useParams();

  const { data } = useQuery({
    queryKey: ['article', articleId],
    queryFn: () =>
      getArticleById({ articleId: Number(articleId), memberId: 1 }),
    enabled: !!articleId,
  });

  if (!data) return null;
  return (
    <PageLayout activeNav="today">
      <h1>Article Detail</h1>
    </PageLayout>
  );
}
