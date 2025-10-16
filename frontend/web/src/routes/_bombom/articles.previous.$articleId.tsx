import { theme } from '@bombom/shared';
import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute, useRouterState } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import ChevronIcon from '@/components/icons/ChevronIcon';
import { useDevice } from '@/hooks/useDevice';
import { useUserInfo } from '@/hooks/useUserInfo';
import {
  extractBodyContent,
  processContent,
} from '@/pages/detail/components/ArticleContent/ArticleContent.utils';
import ArticleHeader from '@/pages/detail/components/ArticleHeader/ArticleHeader';
import PreviousHeader from '@/pages/previous-newsletter/components/PreviousHeader/PreviousHeader';
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
  const device = useDevice();
  const { userInfo } = useUserInfo();
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

  const handleSubscribeClick = () => {
    openSubscribeLink(subscribeUrl, article.newsletter.name, userInfo);
  };

  const handleScrollUp = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <>
      {device !== 'pc' && (
        <PreviousHeader onSubscribeClick={handleSubscribeClick} />
      )}

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

        {device === 'pc' && (
          <ActionButtonWrapper>
            <ActionButton type="button" onClick={handleSubscribeClick}>
              구독
            </ActionButton>

            <ActionButton type="button" onClick={handleScrollUp}>
              <ChevronIcon
                direction="up"
                width={28}
                height={28}
                color={theme.colors.icons}
              />
            </ActionButton>
          </ActionButtonWrapper>
        )}
      </Container>
    </>
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

const ActionButtonWrapper = styled.div`
  position: fixed;
  top: 80vh;
  left: 10%;
  z-index: ${({ theme }) => theme.zIndex.floating};
  width: 56px;
  padding: 4px 0;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 12px;
  box-shadow: 0 2px 8px rgb(0 0 0 / 5%);

  display: flex;
  gap: 8px;
  flex-direction: column;
  align-items: center;

  background-color: ${({ theme }) => theme.colors.dividers};
`;

const ActionButton = styled.button`
  width: 44px;
  height: 44px;
  padding: 8px;
  border-radius: 50%;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.white};

  & > svg {
    transition: transform 0.2s ease;
  }

  &:hover > svg {
    transform: scale(1.1);
  }
`;
