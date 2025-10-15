import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute, useRouterState } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import Button from '@/components/Button/Button';
import { useUserInfo } from '@/hooks/useUserInfo';
import {
  extractBodyContent,
  processContent,
} from '@/pages/detail/components/ArticleContent/ArticleContent.utils';
import ArticleHeader from '@/pages/detail/components/ArticleHeader/ArticleHeader';
import { openSubscribeLink } from '@/pages/recommend/components/NewsletterDetail/NewsletterDetail.utils';

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
  const { userInfo, isLoggedIn } = useUserInfo();
  const { articleId } = Route.useParams();
  const subscribeUrl = useRouterState({
    select: (routerState) => routerState.location.state.subscribeUrl,
  });
  const articleIdNumber = Number(articleId);

  const { data: article } = useQuery(
    queries.previousArticleDetail({ id: articleIdNumber }),
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
      <Divider />

      <SubscribeButton
        disabled={!userInfo}
        onClick={() =>
          openSubscribeLink(subscribeUrl, article.newsletter.name, userInfo)
        }
      >
        {isLoggedIn ? '구독하기' : '로그인 후 구독할 수 있어요'}
      </SubscribeButton>
    </Container>
  );
}

const Container = styled.div`
  max-width: 700px;
  margin: 0 auto;
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

const SubscribeButton = styled(Button)`
  width: 100%;
  height: 48px;
  border-radius: 12px;

  font: ${({ theme }) => theme.fonts.heading6};
`;
