import styled from '@emotion/styled';
import { formatDateToDotString } from '../../../utils/date';
import clockIcon from '../../../../public/assets/clock.svg';
import { Article } from '../types/article';

interface ArticleCardProps {
  data: Article;
}

function ArticleCard({ data }: ArticleCardProps) {
  const {
    title,
    contentsSummary,
    arrivedDateTime,
    thumbnailUrl,
    expectedReadTime,
    isRead,
    newsletter: { name: newsletterName, category: newsletterCategory },
  } = data;

  return (
    <Container isRead={isRead}>
      <InfoWrapper>
        <Title>{title}</Title>
        <Description>{contentsSummary}</Description>
        <MetaInfoRow>
          <MetaInfoTag>{newsletterCategory}</MetaInfoTag>
          <MetaInfoText>from {newsletterName}</MetaInfoText>
          <MetaInfoText>{formatDateToDotString(arrivedDateTime)}</MetaInfoText>
          <ReadTimeBox>
            <img src={clockIcon} alt="시계 아이콘" />
            <MetaInfoText>{`${expectedReadTime}분`}</MetaInfoText>
          </ReadTimeBox>
        </MetaInfoRow>
      </InfoWrapper>
      <Thumbnail src={thumbnailUrl} alt="아티클 썸네일" />
    </Container>
  );
}

export default ArticleCard;

const Container = styled.div<{ isRead: boolean }>`
  display: flex;
  align-items: center;
  gap: 12px;

  padding: 20px;

  border-bottom: ${({ theme, isRead }) =>
    `${isRead ? '0' : '4px'} solid ${theme.colors.primary}`};
  border-radius: 20px;
  box-sizing: border-box;

  box-shadow: 0 20px 25px -5px rgb(0 0 0 / 10%);
  opacity: ${({ isRead }) => (isRead ? 0.5 : 1)};
`;

const InfoWrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;

  width: 100%;
`;

const Title = styled.h2`
  ${({ theme }) => theme.fonts.heading4};
`;

const Description = styled.p`
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;

  overflow: hidden;

  ${({ theme }) => theme.fonts.body2};
  color: ${({ theme }) => theme.colors.textSecondary};
`;

const MetaInfoRow = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

const MetaInfoTag = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;

  padding: 4px 8px;
  border-radius: 16px;

  ${({ theme }) => theme.fonts.caption};
  background-color: ${({ theme }) => theme.colors.dividers};
  color: ${({ theme }) => theme.colors.textPrimary};
`;

const MetaInfoText = styled.span`
  ${({ theme }) => theme.fonts.caption};
  color: ${({ theme }) => theme.colors.textTertiary};
`;

const ReadTimeBox = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
`;

const Thumbnail = styled.img`
  width: 126px;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  align-self: stretch;
  flex-shrink: 0;

  border-radius: 12px;
`;
