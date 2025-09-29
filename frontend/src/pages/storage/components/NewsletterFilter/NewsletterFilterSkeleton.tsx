import {
  Container,
  IconWrapper,
  Title,
  TitleWrapper,
} from './NewsletterFilter';
import SkeletonBox from '@/components/Skeleton/SkeletonBox';
import TabSkeleton from '@/components/Tab/TabSkeleton';
import { useDevice } from '@/hooks/useDevice';
import { theme } from '@/styles/theme';
import NewsIcon from '#/assets/svg/news.svg';

const SKELETON_LENGTH = {
  pc: 6,
  mobile: 5,
};

const TOTAL_COUNT_INDEX = 0;

const NewsletterFilterSkeleton = () => {
  const device = useDevice();
  const isPC = device === 'pc';

  return (
    <Container aria-label="뉴스레터" isPc={device === 'pc'}>
      {device === 'pc' && (
        <TitleWrapper>
          <IconWrapper>
            <NewsIcon width={16} height={16} fill={theme.colors.white} />
          </IconWrapper>
          <Title>뉴스레터</Title>
        </TitleWrapper>
      )}
      {Array.from({
        length: isPC ? SKELETON_LENGTH.pc : SKELETON_LENGTH.mobile,
      }).map((_, index) => (
        <TabSkeleton
          key={index}
          StartComponent={
            index !== TOTAL_COUNT_INDEX &&
            isPC && (
              <SkeletonBox width="24px" height="24px" borderRadius="50%" />
            )
          }
          EndComponent={isPC && <SkeletonBox width="36px" height="24px" />}
          textAlign={isPC ? 'start' : 'center'}
          skeletonWidth="80px"
          skeletonHeight="36px"
        />
      ))}
    </Container>
  );
};

export default NewsletterFilterSkeleton;
