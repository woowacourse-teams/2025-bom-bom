import { keyframes, css } from '@emotion/react';
import styled from '@emotion/styled';
import { InfoWrapper, MetaInfoRow, ThumbnailWrapper } from './ArticleCard';
import { useDevice } from '@/hooks/useDevice';

const ArticleCardSkeleton = () => {
  const device = useDevice();
  const isMobile = device === 'mobile';

  return (
    <SkeletonContainer isMobile={isMobile}>
      <InfoWrapper isMobile={isMobile}>
        <SkeletonTitle isMobile={isMobile} />
        <SkeletonDescription isMobile={isMobile} />
        <MetaInfoRow isMobile={isMobile}>
          <SkeletonMetaInfo />
          <SkeletonMetaInfo />
          <SkeletonMetaInfo />
        </MetaInfoRow>
      </InfoWrapper>
      <ThumbnailWrapper isMobile={isMobile}>
        <SkeletonThumbnail isMobile={isMobile} />
      </ThumbnailWrapper>
    </SkeletonContainer>
  );
};

export default ArticleCardSkeleton;

const shimmer = keyframes`
  0% { background-position: -400px 0; }
  100% { background-position: 400px 0; }
`;

const skeletonShimmerStyle = css`
  background: linear-gradient(90deg, #e0e0e0 25%, #f0f0f0 37%, #e0e0e0 63%);
  background-size: 400px 100%;

  animation: ${shimmer} 1.4s ease infinite;
`;

const SkeletonContainer = styled.div<{ isMobile: boolean }>`
  padding: ${({ isMobile }) => (isMobile ? '8px 0' : '20px')};

  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '8px' : '12px')};
  align-items: center;

  background-color: ${({ theme }) => theme.colors.white};

  ${({ isMobile }) =>
    !isMobile &&
    `
    border-radius: 20px;
    box-shadow: 0 20px 25px -5px rgb(0 0 0 / 10%);
  `};

  cursor: default;
`;

const SkeletonTitle = styled.div<{ isMobile: boolean }>`
  width: 85%;
  height: ${({ isMobile }) => (isMobile ? '20px' : '28px')};
  border-radius: 4px;
  ${skeletonShimmerStyle}
`;

const SkeletonDescription = styled.div<{ isMobile: boolean }>`
  width: 100%;
  height: 16px;
  margin-bottom: 4px;
  border-radius: 4px;
  ${skeletonShimmerStyle}
`;

const SkeletonMetaInfo = styled.div`
  width: 60px;
  height: 12px;
  border-radius: 4px;

  ${skeletonShimmerStyle}
`;

const SkeletonThumbnail = styled.div<{ isMobile: boolean }>`
  width: ${({ isMobile }) => (isMobile ? '64px' : '126px')};
  height: ${({ isMobile }) => (isMobile ? '64px' : '126px')};
  border-radius: ${({ isMobile }) => (isMobile ? '8px' : '12px')};

  flex-shrink: 0;
  ${skeletonShimmerStyle}
`;
