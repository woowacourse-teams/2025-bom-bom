import styled from '@emotion/styled';
import {
  Container,
  InfoWrapper,
  MetaInfoRow,
  ThumbnailWrapper,
} from './ArticleCard';
import Skeleton from '@/components/Skeleton/Skeleton';
import { useDevice } from '@/hooks/useDevice';

const ArticleCardSkeleton = () => {
  const device = useDevice();
  const isMobile = device === 'mobile';

  return (
    <SkeletonContainer isMobile={isMobile} as={Container}>
      <SkeletonInfoWrapper as={InfoWrapper}>
        <Skeleton width="85%" height={isMobile ? '20px' : '28px'} />
        <Skeleton width="100%" height="16px" />
        <SkeletonMetaInfoRow as={MetaInfoRow}>
          <Skeleton width="60px" height="12px" />
          <Skeleton width="60px" height="12px" />
          <Skeleton width="60px" height="12px" />
        </SkeletonMetaInfoRow>
      </SkeletonInfoWrapper>
      <SkeletonThumbnailWrapper as={ThumbnailWrapper}>
        <Skeleton
          width={isMobile ? '64px' : '126px'}
          height={isMobile ? '64px' : '126px'}
        />
      </SkeletonThumbnailWrapper>
    </SkeletonContainer>
  );
};

export default ArticleCardSkeleton;

const SkeletonContainer = styled.div<{ isMobile: boolean }>`
  border-bottom: none;

  ${({ isMobile }) => isMobile && `box-shadow: none`}
`;

const SkeletonInfoWrapper = styled.div``;

const SkeletonMetaInfoRow = styled.div``;

const SkeletonThumbnailWrapper = styled.div``;
