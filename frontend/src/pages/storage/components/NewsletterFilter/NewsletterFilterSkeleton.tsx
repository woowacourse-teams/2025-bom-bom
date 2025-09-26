import styled from '@emotion/styled';
import NewsletterFilterContainer from './NewsletterFilterContainer';
import BadgeSkeleton from '@/components/Badge/BadgeSkeleton';
import TabSkeleton from '@/components/Tab/TabSkeleton';
import { useDevice } from '@/hooks/useDevice';
import { skeletonStyle } from '@/styles/skeleton';

const SKELETON_LENGTH = {
  pc: 6,
  mobile: 5,
};

const TOTAL_COUNT_INDEX = 0;

const NewsletterFilterSkeleton = () => {
  const device = useDevice();
  const isPC = device === 'pc';

  return (
    <NewsletterFilterContainer>
      {Array.from({
        length: isPC ? SKELETON_LENGTH.pc : SKELETON_LENGTH.mobile,
      }).map((_, index) => (
        <TabSkeleton
          key={index}
          StartComponent={
            index !== TOTAL_COUNT_INDEX && isPC && <SkeletonImage />
          }
          EndComponent={isPC && <BadgeSkeleton skeletonHeight="24px" />}
          textAlign={isPC ? 'start' : 'center'}
          skeletonWidth="80px"
          skeletonHeight="36px"
        />
      ))}
    </NewsletterFilterContainer>
  );
};

export default NewsletterFilterSkeleton;

const SkeletonImage = styled.div`
  width: 24px;
  height: 24px;
  border-radius: 50%;

  flex-shrink: 0;

  ${skeletonStyle}
`;
