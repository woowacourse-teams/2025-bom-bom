import styled from '@emotion/styled';
import { Link } from '@tanstack/react-router';
import Badge from '@/components/Badge/Badge';
import Chip from '@/components/Chip/Chip';
import ImageWithFallback from '@/components/ImageWithFallback/ImageWithFallback';
import { components } from '@/types/openapi';
import { formatDate } from '@/utils/date';
import clockIcon from '#/assets/clock.svg';

type ReadVariantType = 'transparent' | 'badge';

interface ArticleCardProps {
  data: components['schemas']['ArticleResponse'];
  readVariant?: ReadVariantType;
}

function ArticleCard({ data, readVariant = 'transparent' }: ArticleCardProps) {
  const {
    articleId,
    title,
    contentsSummary,
    arrivedDateTime,
    thumbnailUrl,
    expectedReadTime,
    isRead,
    newsletter,
  } = data;

  return (
    <Container
      isRead={isRead ?? false}
      readVariant={readVariant}
      to={`/articles/${articleId}`}
    >
      <InfoWrapper>
        <Title>{title}</Title>
        <Description>{contentsSummary || title}</Description>
        <MetaInfoRow>
          <Chip text={newsletter?.category ?? ''} />
          <MetaInfoText>from {newsletter?.name ?? ''}</MetaInfoText>
          <MetaInfoText>
            {formatDate(new Date(arrivedDateTime ?? ''))}
          </MetaInfoText>
          <ReadTimeBox>
            <img src={clockIcon} alt="시계 아이콘" />
            <MetaInfoText>{`${expectedReadTime}분`}</MetaInfoText>
          </ReadTimeBox>
        </MetaInfoRow>
      </InfoWrapper>
      <ThumbnailWrapper>
        <Thumbnail
          src={thumbnailUrl ?? newsletter?.imageUrl ?? ''}
          alt="아티클 썸네일"
        />
        {isRead && readVariant === 'badge' && (
          <BadgeWrapper>
            <Badge text="읽음" variant="outlinePrimary" />
          </BadgeWrapper>
        )}
      </ThumbnailWrapper>
    </Container>
  );
}

export default ArticleCard;

const Container = styled(Link)<{
  isRead: boolean;
  readVariant: ReadVariantType;
}>`
  display: flex;
  gap: 12px;
  align-items: center;

  padding: 20px;
  border-bottom: ${({ theme, isRead }) =>
    `${isRead ? '0' : '4px'} solid ${theme.colors.primary}`};
  border-radius: 20px;
  box-shadow: 0 20px 25px -5px rgb(0 0 0 / 10%);

  background-color: ${({ theme }) => theme.colors.white};

  color: inherit;

  box-sizing: border-box;

  opacity: ${({ isRead, readVariant }) =>
    isRead && readVariant === 'transparent' ? 0.5 : 1};

  text-decoration: none;
`;

const InfoWrapper = styled.div`
  display: flex;
  gap: 12px;
  flex-direction: column;
  align-items: flex-start;

  width: 100%;
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

const ThumbnailWrapper = styled.div`
  position: relative;
`;

const Thumbnail = styled(ImageWithFallback)`
  flex-shrink: 0;
  align-self: stretch;

  width: 126px;
  border-radius: 12px;

  aspect-ratio: 1 / 1;
  object-fit: cover;
`;

const BadgeWrapper = styled.div`
  position: absolute;
  top: 4px;
  right: 4px;
`;
