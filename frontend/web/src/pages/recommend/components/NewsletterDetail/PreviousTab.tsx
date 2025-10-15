import { theme } from '@bombom/shared';
import styled from '@emotion/styled';
import { useNavigate } from '@tanstack/react-router';
import PreviousArticleListItem from './PreviousArticleListItem';
import Button from '@/components/Button/Button';
import { openExternalLink } from '@/utils/externalLink';
import type { GetPreviousArticlesResponse } from '@/apis/previousArticles';
import OpenIcon from '#/assets/svg/open.svg';

interface PreviousTabProps {
  previousArticles?: GetPreviousArticlesResponse | null;
  previousNewsletterUrl?: string;
  isMobile: boolean;
}

const PreviousTab = ({
  previousArticles,
  previousNewsletterUrl,
  isMobile,
}: PreviousTabProps) => {
  const navigate = useNavigate();

  const openPreviousLetters = () => {
    if (!previousNewsletterUrl) return;
    openExternalLink(previousNewsletterUrl);
  };

  if (previousArticles?.length) {
    return previousArticles.map((article) => (
      <PreviousArticleListItem
        key={article.articleId}
        title={article.title}
        contentsSummary={article.contentsSummary}
        expectedReadTime={article.expectedReadTime}
        onClick={() =>
          navigate({ to: `articles/previous/${article.articleId}` })
        }
      />
    ));
  }

  if (previousNewsletterUrl) {
    return (
      <Container>
        <Button onClick={openPreviousLetters}>
          지난 소식 보러가기
          <OpenIcon fill={theme.colors.white} width={16} height={16} />
        </Button>
      </Container>
    );
  }

  return (
    <Container>
      <EmptyState isMobile={isMobile}>
        해당 뉴스레터는 지난 뉴스레터가 존재하지 않습니다.
      </EmptyState>
    </Container>
  );
};

export default PreviousTab;

const Container = styled.div`
  padding: 24px 0;

  display: flex;
  justify-content: center;
`;

const EmptyState = styled.p<{ isMobile: boolean }>`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body3 : theme.fonts.body2};
`;
