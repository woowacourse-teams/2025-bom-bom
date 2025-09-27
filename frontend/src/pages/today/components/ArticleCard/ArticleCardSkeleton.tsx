import styled from '@emotion/styled';
import {
  Container,
  Description,
  InfoWrapper,
  MetaInfoRow,
  MetaInfoText,
  ThumbnailWrapper,
  Title,
} from './ArticleCard';
import SkeletonBox from '@/components/Skeleton/SkeletonBox';
import { useDevice } from '@/hooks/useDevice';
import { skeletonStyle } from '@/styles/skeleton';

const ArticleCardSkeleton = () => {
  const device = useDevice();
  const isMobile = device === 'mobile';

  return (
    <SkeletonContainer as={Container} isMobile={isMobile}>
      <SkeletonInfoWrapper as={InfoWrapper} isMobile={isMobile}>
        <SkeletonTitle as={Title} isMobile={isMobile} />
        <SkeletonDescription as={Description} isMobile={isMobile} />
        <SkeletonMetaInfoRow as={MetaInfoRow} isMobile={isMobile}>
          <SkeletonMetaInfo as={MetaInfoText} />
          <SkeletonMetaInfo as={MetaInfoText} />
          <SkeletonMetaInfo as={MetaInfoText} />
        </SkeletonMetaInfoRow>
      </SkeletonInfoWrapper>
      <SkeletonThumbnailWrapper as={ThumbnailWrapper} isMobile={isMobile}>
        <SkeletonBox
          width={isMobile ? '64px' : '126px'}
          height={isMobile ? '64px' : '126px'}
        />
      </SkeletonThumbnailWrapper>
    </SkeletonContainer>
  );
};

export default ArticleCardSkeleton;

const SkeletonContainer = styled.div<{ isMobile: boolean }>`
  border-bottom: 0;
`;

const SkeletonTitle = styled.div<{ isMobile: boolean }>`
  width: 85%;
  height: ${({ isMobile }) => (isMobile ? '20px' : '28px')};
  border-radius: 4px;

  ${skeletonStyle};
`;

const SkeletonDescription = styled.div<{ isMobile: boolean }>`
  width: 100%;
  height: 16px;

  ${skeletonStyle}
`;

const SkeletonMetaInfo = styled.div`
  width: 60px;
  height: 12px;
  border-radius: 4px;

  ${skeletonStyle}
`;

const SkeletonInfoWrapper = styled.div<{ isMobile: boolean }>`
  width: 100%;
`;

const SkeletonMetaInfoRow = styled.div<{ isMobile: boolean }>``;

const SkeletonThumbnailWrapper = styled.div<{ isMobile: boolean }>``;
