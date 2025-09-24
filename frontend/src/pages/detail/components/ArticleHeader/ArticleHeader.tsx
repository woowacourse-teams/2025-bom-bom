import styled from '@emotion/styled';
import Chip from '@/components/Chip/Chip';
import { Device, useDevice } from '@/hooks/useDevice';
import { formatDate } from '@/utils/date';
import ClockIcon from '#/assets/svg/clock.svg';

interface ArticleHeaderProps {
  title: string;
  newsletterCategory: string;
  newsletterName: string;
  arrivedDateTime: Date;
  expectedReadTime: number;
  bookmarked?: boolean;
  onBookmarkClick?: (bookmarked: boolean) => void;
}

const ArticleHeader = ({
  title,
  newsletterCategory,
  newsletterName,
  arrivedDateTime,
  expectedReadTime,
}: ArticleHeaderProps) => {
  const device = useDevice();

  return (
    <Container>
      <TitleRow>
        <Title device={device}>{title}</Title>
      </TitleRow>
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
  padding: 20px 0;

  display: flex;
  gap: 12px;
  flex-direction: column;
  align-items: flex-start;
  align-self: stretch;
`;

const TitleRow = styled.div`
  width: 100%;

  display: flex;
  gap: 12px;
  align-items: flex-start;
`;

const Title = styled.h2<{ device: Device }>`
  flex: 1;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme, device }) =>
    device === 'mobile' ? theme.fonts.heading4 : theme.fonts.heading3};
`;

const MetaInfoRow = styled.div`
  width: 100%;

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
