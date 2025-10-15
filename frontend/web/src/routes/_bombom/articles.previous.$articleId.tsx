import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import {
  extractBodyContent,
  processContent,
} from '@/pages/detail/components/ArticleContent/ArticleContent.utils';
import ArticleHeader from '@/pages/detail/components/ArticleHeader/ArticleHeader';

export const Route = createFileRoute('/_bombom/articles/previous/$articleId')({
  head: () => ({
    meta: [
      {
        name: 'robots',
        content: 'noindex, nofollow',
      },
      {
        title: '봄봄 | 지난 뉴스레터 상세',
      },
    ],
  }),
  component: RouteComponent,
});

function RouteComponent() {
  const { articleId } = Route.useParams();
  const articleIdNumber = Number(articleId);

  const { data: article } = useQuery(
    queries.previousArticleDetail({ articleId: articleIdNumber }),
  );
  const bodyContent = extractBodyContent(article?.contents ?? '');

  if (!article) return null;

  return (
    <Container>
      <ArticleHeader
        title={article.title}
        newsletterCategory={article.newsletter.category}
        newsletterName={article.newsletter.name}
        arrivedDateTime={new Date(article.arrivedDateTime)}
        expectedReadTime={article.expectedReadTime}
      />
      <Divider />

      <Content
        dangerouslySetInnerHTML={{
          __html: processContent(article.newsletter.name, bodyContent),
        }}
      />
    </Container>
  );
}

const Container = styled.div`
  width: 100%;
  max-width: 700px;
  margin: 0 auto;
  margin-top: 20px;
  padding: 28px;
  border-right: 1px solid ${({ theme }) => theme.colors.stroke};
  border-left: 1px solid ${({ theme }) => theme.colors.stroke};

  display: flex;
  gap: 20px;
  flex-direction: column;
  align-items: center;
`;

const Divider = styled.div`
  width: 100%;
  height: 1px;

  background-color: ${({ theme }) => theme.colors.dividers};
`;

const Content = styled.div`
  overflow: visible;

  display: flex;
  flex-direction: column;
  align-items: flex-start;

  white-space: pre-line;

  -webkit-tap-highlight-color: rgb(0 0 0 / 10%);
  -webkit-touch-callout: default;

  user-select: text;

  word-break: break-all;
  word-wrap: break-word;

  a {
    color: ${({ theme }) => theme.colors.info};

    cursor: pointer;
    text-decoration: underline;

    &:hover {
      text-decoration: none;
    }
  }
`;
