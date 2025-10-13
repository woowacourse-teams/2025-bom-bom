import styled from '@emotion/styled';
import ClockIcon from '#/assets/svg/clock.svg';

interface PreviousArticleListItemProps {
  articleId: number;
  title: string;
  contentsSummary: string;
  expectedReadTime: number;
  onClick?: (articleId: number) => void;
}

const PreviousArticleListItem = ({
  articleId,
  title,
  contentsSummary,
  expectedReadTime,
  onClick,
}: PreviousArticleListItemProps) => {
  return (
    <Container onClick={() => onClick?.(articleId)}>
      <InfoWrapper>
        <Title>{title}</Title>
        <Description>{contentsSummary}</Description>
        <MetaInfoRow>
          <ReadTimeBox>
            <ClockIcon width={16} height={16} />
            <MetaInfoText>{`${expectedReadTime}분 소요`}</MetaInfoText>
          </ReadTimeBox>
        </MetaInfoRow>
      </InfoWrapper>
    </Container>
  );
};

export default PreviousArticleListItem;

const Container = styled.div`
  padding: 16px;
  border-bottom: 1px solid ${({ theme }) => theme.colors.dividers};

  display: flex;
  flex-direction: column;
  gap: 8px;

  cursor: pointer;
  transition: background 0.2s ease;

  &:hover {
    background: ${({ theme }) => theme.colors.backgroundHover};
  }
`;

const InfoWrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 6px;
`;

const Title = styled.h3`
  font: ${({ theme }) => theme.fonts.heading6};
  color: ${({ theme }) => theme.colors.textPrimary};

  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
`;

const Description = styled.p`
  font: ${({ theme }) => theme.fonts.body2};
  color: ${({ theme }) => theme.colors.textSecondary};

  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
`;

const MetaInfoRow = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

const MetaInfoText = styled.span`
  font: ${({ theme }) => theme.fonts.caption};
  color: ${({ theme }) => theme.colors.textTertiary};
`;

const ReadTimeBox = styled.div`
  display: flex;
  gap: 4px;
  align-items: center;
`;
