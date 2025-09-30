import styled from '@emotion/styled';
import {
  Container,
  InfoWrapper,
  MetaInfoRow,
  ThumbnailWrapper,
} from './ArticleCard';
import SkeletonBox from '@/components/Skeleton/SkeletonBox';
import { useDevice } from '@/hooks/useDevice';

const ArticleCardSkeleton = () => {
  const device = useDevice();
  const isMobile = device === 'mobile';

  return (
    <SkeletonContainer as={Container}>
      <SkeletonInfoWrapper as={InfoWrapper}>
        <SkeletonBox width="85%" height={isMobile ? '20px' : '28px'} />
        <SkeletonBox width="100%" height="16px" />
        <SkeletonMetaInfoRow as={MetaInfoRow}>
          <SkeletonBox width="60px" height="12px" />
          <SkeletonBox width="60px" height="12px" />
          <SkeletonBox width="60px" height="12px" />
        </SkeletonMetaInfoRow>
      </SkeletonInfoWrapper>
      <SkeletonThumbnailWrapper as={ThumbnailWrapper}>
        <SkeletonBox
          width={isMobile ? '64px' : '126px'}
          height={isMobile ? '64px' : '126px'}
        />
      </SkeletonThumbnailWrapper>
    </SkeletonContainer>
  );
};

export default ArticleCardSkeleton;

const SkeletonContainer = styled.div``;

const SkeletonInfoWrapper = styled.div``;

const SkeletonMetaInfoRow = styled.div``;

const SkeletonThumbnailWrapper = styled.div``;
