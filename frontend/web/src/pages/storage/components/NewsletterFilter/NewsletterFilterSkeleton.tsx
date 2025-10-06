import { theme } from '@bom-bom/shared';
import {
  Container,
  IconWrapper,
  StyledTabs,
  Title,
  TitleWrapper,
} from './NewsletterFilter';
import TabSkeleton from '@/components/Tab/TabSkeleton';
import { useDevice } from '@/hooks/useDevice';
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
    <Container aria-label="뉴스레터" isPc={isPC}>
      {isPC && (
        <TitleWrapper>
          <IconWrapper>
            <NewsIcon width={16} height={16} fill={theme.colors.white} />
          </IconWrapper>
          <Title>뉴스레터</Title>
        </TitleWrapper>
      )}
      <StyledTabs direction={device === 'pc' ? 'vertical' : 'horizontal'}>
        {Array.from({
          length: isPC ? SKELETON_LENGTH.pc : SKELETON_LENGTH.mobile,
        }).map((_, index) => (
          <TabSkeleton
            key={index}
            StartComponentSkeleton={
              index !== TOTAL_COUNT_INDEX
                ? { width: '24px', height: '24px', borderRadius: '50%' }
                : undefined
            }
            EndComponentSkeleton={
              isPC ? { width: '36px', height: '24px' } : undefined
            }
            textAlign={isPC ? 'start' : 'center'}
            width="80px"
            height="36px"
          />
        ))}
      </StyledTabs>
    </Container>
  );
};

export default NewsletterFilterSkeleton;
