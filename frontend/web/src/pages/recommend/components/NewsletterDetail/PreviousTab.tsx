import { useQuery } from '@tanstack/react-query';
import { useNavigate } from '@tanstack/react-router';
import PreviousArticleListItem from './PreviousArticleListItem';
import { queries } from '@/apis/queries';

interface PreviousTabProps {
  newsletterId: number;
}

const PreviousTab = ({ newsletterId }: PreviousTabProps) => {
  const navigate = useNavigate();
  const { data: previousArticles } = useQuery({
    ...queries.previousArticles({ newsletterId, limit: 10 }),
  });

  return (
    <>
      {previousArticles?.map((article) => (
        <PreviousArticleListItem
          key={article.articleId}
          title={article.title}
          contentsSummary={article.contentsSummary}
          expectedReadTime={article.expectedReadTime}
          onClick={() =>
            navigate({ to: `articles/previous/${article.articleId}` })
          }
        />
      ))}
    </>
  );
};

export default PreviousTab;
