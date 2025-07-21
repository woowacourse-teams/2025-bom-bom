import styled from '@emotion/styled';
import { formatDateToDotString } from '../../../../utils/date';
import clockIcon from '../../../../../public/assets/clock.svg';
import { Article } from '../../types/article';
import { Link } from '@tanstack/react-router';

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
    newsletter: {
      name: newsletterName,
      category: newsletterCategory,
      imageUrl: newsletterImageUrl,
    },
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
      <Thumbnail src={thumbnailUrl ?? newsletterImageUrl} alt="아티클 썸네일" />
    </Container>
  );
}

export default ArticleCard;

const Container = styled(Link)<{ isRead: boolean }>`
  display: flex;
  align-items: center;

  padding: 20px;
  border-bottom: ${({ theme, isRead }) =>
    `${isRead ? '0' : '4px'} solid ${theme.colors.primary}`};
  border-radius: 20px;
  box-shadow: 0 20px 25px -5px rgb(0 0 0 / 10%);

  background-color: ${({ theme }) => theme.colors.white};

  color: inherit;

  box-sizing: border-box;
  gap: 12px;
  opacity: ${({ isRead }) => (isRead ? 0.5 : 1)};

  text-decoration: none;
`;

const InfoWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  width: 100%;

  gap: 12px;
`;

const Title = styled.h2`
  font: ${({ theme }) => theme.fonts.heading4};
`;

const Description = styled.p`
  overflow: hidden;

  display: -webkit-box;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};

  -webkit-box-orient: vertical;

  -webkit-line-clamp: 2;
`;

const MetaInfoRow = styled.div`
  display: flex;
  align-items: center;

  gap: 8px;
`;

const MetaInfoTag = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;

  padding: 4px 8px;
  border-radius: 16px;

  background-color: ${({ theme }) => theme.colors.dividers};

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const MetaInfoText = styled.span`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const ReadTimeBox = styled.div`
  display: flex;
  align-items: center;

  gap: 4px;
`;

const Thumbnail = styled.img`
  flex-shrink: 0;
  align-self: stretch;

  width: 126px;
  border-radius: 12px;

  aspect-ratio: 1 / 1;
  object-fit: cover;
`;
