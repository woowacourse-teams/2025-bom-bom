import styled from '@emotion/styled';
import Chip from '@/components/Chip/Chip';
import { formatDate } from '@/utils/date';
import ClockIcon from '#/assets/clock.svg';

interface ArticleHeaderProps {
  title: string;
  newsletterCategory: string;
  newsletterName: string;
  arrivedDateTime: Date;
  expectedReadTime: number;
}

const ArticleHeader = ({
  title,
  newsletterCategory,
  newsletterName,
  arrivedDateTime,
  expectedReadTime,
}: ArticleHeaderProps) => {
  return (
    <Container>
      <Title>{title}</Title>
      <MetaInfoRow>
        <Chip text={newsletterCategory} />
        <MetaInfoText>from {newsletterName}</MetaInfoText>
        <MetaInfoText>{formatDate(arrivedDateTime)}</MetaInfoText>
        <ReadTimeBox>
          <ClockIcon width={16} height={16} />
          <MetaInfoText>{expectedReadTime}분</MetaInfoText>
        </ReadTimeBox>
      </MetaInfoRow>
    </Container>
  );
};

export default ArticleHeader;

const Container = styled.div`
  display: flex;
  gap: 12px;
  flex-direction: column;
  align-items: flex-start;
  align-self: stretch;
`;

const Title = styled.h2`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading2};
`;

const MetaInfoRow = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const MetaInfoText = styled.span`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const ReadTimeBox = styled.div`
  display: flex;
  gap: 4px;
  align-items: center;
`;
