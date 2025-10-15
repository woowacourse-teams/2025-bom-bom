import { theme } from '@bombom/shared';
import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from '@tanstack/react-router';
import PreviousArticleListItem from './PreviousArticleListItem';
import { queries } from '@/apis/queries';
import { openExternalLink } from '@/utils/externalLink';
import OpenIcon from '#/assets/svg/open.svg';

interface PreviousTabProps {
  newsletterId: number;
  previousNewsletterUrl?: string;
  isMobile: boolean;
}

const PreviousTab = ({
  newsletterId,
  previousNewsletterUrl,
  isMobile,
}: PreviousTabProps) => {
  const navigate = useNavigate();
  const { data: previousArticles } = useQuery({
    ...queries.previousArticles({ newsletterId, limit: 10 }),
  });

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
        <LinkButton onClick={openPreviousLetters} isMobile={isMobile}>
          지난 소식 보러가기
          <OpenIcon fill={theme.colors.white} width={16} height={16} />
        </LinkButton>
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

const LinkButton = styled.button<{ isMobile: boolean }>`
  padding: 10px 16px;
  border-radius: 8px;

  display: inline-flex;
  gap: 6px;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body3 : theme.fonts.body2};

  cursor: pointer;

  transition: background-color 0.2s ease;

  &:hover {
    background-color: ${({ theme }) => theme.colors.primaryDark};
  }
`;
