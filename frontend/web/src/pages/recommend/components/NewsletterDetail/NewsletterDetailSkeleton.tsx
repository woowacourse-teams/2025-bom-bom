import styled from '@emotion/styled';
import {
  Container,
  FixedWrapper,
  InfoBox,
  InfoWrapper,
  NewsletterInfo,
  ScrollableWrapper,
  TitleWrapper,
} from './NewsletterDetail';
import Skeleton from '@/components/Skeleton/Skeleton';
import { useDevice } from '@/hooks/useDevice';

const NewsletterDetailSkeleton = () => {
  const deviceType = useDevice();
  const isMobile = deviceType === 'mobile';

  return (
    <Container isMobile={isMobile}>
      <FixedWrapper isMobile={isMobile}>
        <InfoWrapper isMobile={isMobile}>
          <Skeleton
            width={isMobile ? '88px' : '104px'}
            height={isMobile ? '88px' : '104px'}
            borderRadius={isMobile ? '12px' : '16px'}
          />
          <InfoBox>
            <TitleWrapper isMobile={isMobile}>
              <Skeleton width="60%" height={isMobile ? '20px' : '24px'} />
            </TitleWrapper>

            <NewsletterInfo isMobile={isMobile}>
              <Skeleton width="60px" height={isMobile ? '16px' : '18px'} />
              <Skeleton width="80px" height={isMobile ? '16px' : '18px'} />
            </NewsletterInfo>
          </InfoBox>
        </InfoWrapper>

        <Skeleton
          width="100%"
          height={isMobile ? '28px' : '36px'}
          maxWidth="400px"
          borderRadius="12px"
          alignSelf="center"
        />
      </FixedWrapper>

      <TabSkeletonWrapper>
        <TabSkeletonBox>
          <Skeleton width="80px" height="20px" />
        </TabSkeletonBox>
        <TabSkeletonBox>
          <Skeleton width="80px" height="20px" />
        </TabSkeletonBox>
      </TabSkeletonWrapper>

      <ScrollableWrapper isMobile={isMobile}>
        <Skeleton width="100%" height="100%" />
      </ScrollableWrapper>
    </Container>
  );
};

export default NewsletterDetailSkeleton;

const TabSkeletonWrapper = styled.div`
  border-bottom: 1px solid ${({ theme }) => theme.colors.dividers};
  display: flex;
`;

const TabSkeletonBox = styled.div`
  padding: 12px 0;

  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
`;
