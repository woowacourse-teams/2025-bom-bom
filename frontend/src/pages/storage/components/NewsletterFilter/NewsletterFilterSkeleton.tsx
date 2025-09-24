import styled from '@emotion/styled';
import NewsletterFilterContainer from './NewsletterFilterContainer';
import BadgeSkeleton from '@/components/Badge/BadgeSkeleton';
import TabSkeleton from '@/components/Tab/TabSkeleton';
import { useDevice } from '@/hooks/useDevice';
import { skeletonStyle } from '@/styles/skeleton';

const SKELETON_LENGTH = {
  pc: 6,
  mobile: 4,
};

const NewsletterFilterSkeleton = () => {
  const device = useDevice();
  const isPc = device === 'pc';

  return (
    <NewsletterFilterContainer>
      {Array.from({
        length: isPc ? SKELETON_LENGTH.pc : SKELETON_LENGTH.mobile,
      }).map((_, index) => (
        <TabSkeleton
          key={index}
          StartComponent={index === 0 ? null : <SkeletonImage />}
          EndComponent={<BadgeSkeleton skeletonHeight="24px" />}
          textAlign={isPc ? 'start' : 'center'}
          skeletonHeight="24px"
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
