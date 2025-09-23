import styled from '@emotion/styled';
import { Link } from '@tanstack/react-router';
import { ComponentProps } from 'react';
import Badge from '@/components/Badge/Badge';
import ImageWithFallback from '@/components/ImageWithFallback/ImageWithFallback';
import { useDevice } from '@/hooks/useDevice';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import { components } from '@/types/openapi';
import { formatDate } from '@/utils/date';
import ClockIcon from '#/assets/clock.svg';

type ReadVariant = 'transparent' | 'badge';

interface ArticleCardProps extends ComponentProps<typeof Link> {
  data: components['schemas']['ArticleResponse'];
  readVariant?: ReadVariant;
}

function ArticleCard({
  data,
  readVariant = 'transparent',
  to,
  ...props
}: ArticleCardProps) {
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
  const device = useDevice();
  const isMobile = device === 'mobile';

  return (
    <Container
      isRead={isRead ?? false}
      readVariant={readVariant}
      isMobile={isMobile}
      to={to ?? `/articles/${articleId}`}
      onClick={() => {
        trackEvent({
          category: 'Article',
          action: 'Click Article Card',
          label: `${newsletter?.name} - [${articleId}]${title}`,
        });
      }}
      {...props}
    >
      <InfoWrapper isMobile={isMobile}>
        <Title isMobile={isMobile}>{title}</Title>
        <Description isMobile={isMobile}>
          {contentsSummary || title}
        </Description>
        <MetaInfoRow isMobile={isMobile}>
          <MetaInfoText>from {newsletter?.name ?? ''}</MetaInfoText>
          <MetaInfoText>
            {formatDate(new Date(arrivedDateTime ?? ''))}
          </MetaInfoText>
          <ReadTimeBox>
            <ClockIcon width={16} height={16} />
            <MetaInfoText>{`${expectedReadTime}분`}</MetaInfoText>
          </ReadTimeBox>
        </MetaInfoRow>
      </InfoWrapper>
      <ThumbnailWrapper isMobile={isMobile}>
        <Thumbnail
          isMobile={isMobile}
          src={thumbnailUrl ?? newsletter?.imageUrl ?? ''}
          alt="아티클 썸네일"
        />
        {isRead && readVariant === 'badge' && (
          <ReadingBadge
            text="읽음"
            variant="outlinePrimary"
            isMobile={isMobile}
          />
        )}
      </ThumbnailWrapper>
    </Container>
  );
}

export default ArticleCard;

const Container = styled(Link, {
  shouldForwardProp: (prop) =>
    prop !== 'isRead' && prop !== 'readVariant' && prop !== 'isMobile',
})<{
  isRead: boolean;
  readVariant: ReadVariant;
  isMobile: boolean;
}>`
  padding: ${({ isMobile }) => (isMobile ? '8px 0' : '20px')};

  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '8px' : '12px')};
  align-items: center;

  background-color: ${({ theme }) => theme.colors.white};
  color: inherit;

  box-sizing: border-box;

  opacity: ${({ isRead, readVariant }) =>
    isRead && readVariant === 'transparent' ? 0.5 : 1};

  text-decoration: none;

  ${({ isMobile, isRead, theme }) =>
    !isMobile &&
    `
    border-bottom: ${isRead ? '0' : '4px'} solid ${theme.colors.primary};
    border-radius: 20px;
    box-shadow: 0 20px 25px -5px rgb(0 0 0 / 10%);
  `};
`;

const InfoWrapper = styled.div<{ isMobile: boolean }>`
  width: 100%;

  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '8px' : '12px')};
  flex-direction: column;
  align-items: flex-start;
`;

const Title = styled.h2<{ isMobile: boolean }>`
  overflow: hidden;
  min-height: fit-content;

  display: -webkit-box;

  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body1 : theme.fonts.heading4};
  font-weight: 600;

  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
`;

const Description = styled.p<{ isMobile: boolean }>`
  overflow: hidden;

  display: -webkit-box;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};

  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
`;

const MetaInfoRow = styled.div<{ isMobile: boolean }>`
  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '6px' : '8px')};
  flex-wrap: ${({ isMobile }) => (isMobile ? 'wrap' : 'nowrap')};
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

const ThumbnailWrapper = styled.div<{ isMobile: boolean }>`
  position: relative;
  flex-shrink: 0;
`;

const Thumbnail = styled(ImageWithFallback, {
  shouldForwardProp: (prop) => prop !== 'isMobile',
})<{ isMobile: boolean }>`
  width: ${({ isMobile }) => (isMobile ? '64px' : '126px')};
  border-radius: ${({ isMobile }) => (isMobile ? '8px' : '12px')};

  flex-shrink: 0;
  align-self: stretch;

  aspect-ratio: 1 / 1;
  object-fit: cover;
`;

const ReadingBadge = styled(Badge)<{ isMobile: boolean }>`
  position: absolute;
  top: 4px;
  right: 4px;
  padding: ${({ isMobile }) => (isMobile ? '2px 4px' : '4px 8px')};
`;
