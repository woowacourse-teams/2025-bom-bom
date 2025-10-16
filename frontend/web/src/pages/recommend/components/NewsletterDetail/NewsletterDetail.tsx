import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useQueryState } from 'nuqs';
import DetailTab from './DetailTab';
import { openSubscribeLink } from './NewsletterDetail.utils';
import NewsletterTabs from './NewsletterTabs';
import PreviousTab from './PreviousTab';
import { queries } from '@/apis/queries';
import Badge from '@/components/Badge/Badge';
import Button from '@/components/Button/Button';
import ImageWithFallback from '@/components/ImageWithFallback/ImageWithFallback';
import { useDevice } from '@/hooks/useDevice';
import { useUserInfo } from '@/hooks/useUserInfo';
import { openExternalLink } from '@/utils/externalLink';
import type { NewsletterTab } from './NewsletterDetail.types';
import HomeIcon from '#/assets/svg/home.svg';

interface NewsletterDetailProps {
  newsletterId: number;
}

const NewsletterDetail = ({ newsletterId }: NewsletterDetailProps) => {
  const deviceType = useDevice();
  const { userInfo, isLoggedIn } = useUserInfo();
  const [activeTab, setActiveTab] = useQueryState('tab', {
    defaultValue: 'detail',
  });
  const { data: newsletterDetail } = useQuery({
    ...queries.newsletterDetail({ id: newsletterId }),
    enabled: Boolean(newsletterId),
  });
  const { data: previousArticles } = useQuery({
    ...queries.previousArticles({ newsletterId, limit: 10 }),
  });

  const isMobile = deviceType === 'mobile';

  if (!newsletterId || !newsletterDetail) return null;

  const openMainSite = () => {
    openExternalLink(newsletterDetail.mainPageUrl);
  };

  return (
    <Container>
      <FixedWrapper isMobile={isMobile}>
        <InfoWrapper isMobile={isMobile}>
          <NewsletterImage
            src={newsletterDetail.imageUrl}
            alt={`${newsletterDetail.name} 뉴스레터 이미지`}
            isMobile={isMobile}
          />
          <InfoBox>
            <TitleWrapper isMobile={isMobile}>
              <NewsletterTitle isMobile={isMobile}>
                {newsletterDetail.name}
              </NewsletterTitle>
              <DetailLink onClick={openMainSite} isMobile={isMobile}>
                <StyledHomeIcon isMobile={isMobile} />
              </DetailLink>
            </TitleWrapper>

            <NewsletterInfo isMobile={isMobile}>
              <StyledBadge text={'dfd'} isMobile={isMobile} />
              <IssueCycle>{`${newsletterDetail.issueCycle} 발행`}</IssueCycle>
            </NewsletterInfo>
          </InfoBox>
        </InfoWrapper>

        <SubscribeButton
          text={isLoggedIn ? '구독하기' : '로그인 후 구독할 수 있어요'}
          onClick={() =>
            openSubscribeLink(
              newsletterDetail.subscribeUrl,
              newsletterDetail.name,
              userInfo,
            )
          }
          disabled={!isLoggedIn}
          isMobile={isMobile}
        />
      </FixedWrapper>

      <NewsletterTabs
        activeTab={activeTab as NewsletterTab}
        onTabChange={(newTab) => setActiveTab(newTab)}
      />

      <ScrollableWrapper isMobile={isMobile}>
        {activeTab === 'detail' && (
          <DetailTab
            newsletterDescription={newsletterDetail.description}
            isMobile={isMobile}
          />
        )}

        {activeTab === 'previous' && (
          <PreviousTab
            previousArticles={previousArticles}
            previousNewsletterUrl={newsletterDetail.previousNewsletterUrl}
            newsletterSubscribeUrl={newsletterDetail.subscribeUrl}
            isMobile={isMobile}
          />
        )}
      </ScrollableWrapper>
    </Container>
  );
};

export default NewsletterDetail;

const Container = styled.div`
  width: 100%;
  height: 100%;
  max-width: 560px;

  display: flex;
  flex-direction: column;
`;

const FixedWrapper = styled.div<{ isMobile: boolean }>`
  padding-bottom: ${({ isMobile }) => (isMobile ? '16px' : '24px')};

  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '16px' : '24px')};
  flex-direction: column;
`;

const ScrollableWrapper = styled.div<{ isMobile: boolean }>`
  margin-right: -16px;
  padding: 8px;

  overflow-y: auto;
  scrollbar-gutter: stable;
`;

const InfoWrapper = styled.div<{ isMobile: boolean }>`
  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '12px' : '16px')};
  align-items: center;
  justify-content: center;
`;

const NewsletterImage = styled(ImageWithFallback, {
  shouldForwardProp: (prop) => prop !== 'isMobile',
})<{ isMobile: boolean }>`
  width: ${({ isMobile }) => (isMobile ? '88px' : '104px')};
  height: ${({ isMobile }) => (isMobile ? '88px' : '104px')};
  border-radius: ${({ isMobile }) => (isMobile ? '12px' : '16px')};

  flex-shrink: 0;

  object-fit: cover;
`;

const InfoBox = styled.div`
  width: 100%;

  display: flex;
  gap: 8px;
  flex-direction: column;
`;

const TitleWrapper = styled.div<{ isMobile: boolean }>`
  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '4px' : '8px')};
`;

const NewsletterTitle = styled.h2<{ isMobile: boolean }>`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.heading4 : theme.fonts.heading3};
`;

const StyledHomeIcon = styled(HomeIcon)<{ isMobile: boolean }>`
  width: ${({ isMobile }) => (isMobile ? '20px' : '24px')};
  height: ${({ isMobile }) => (isMobile ? '20px' : '24px')};

  fill: ${({ theme }) => theme.colors.primary};
`;

const NewsletterInfo = styled.div<{ isMobile: boolean }>`
  display: flex;
  gap: 12px;
  align-items: center;

  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body3 : theme.fonts.body2};
`;

const StyledBadge = styled(Badge)<{ isMobile: boolean }>`
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body3 : theme.fonts.body2};
`;

const IssueCycle = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  text-align: center;
`;

const DetailLink = styled.button<{ isMobile: boolean }>`
  display: flex;
  gap: 4px;
  align-items: center;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body3 : theme.fonts.body2};

  transition: all 0.2s ease;

  &:hover {
    color: ${({ theme }) => theme.colors.primary};
    cursor: pointer;
  }
`;

const SubscribeButton = styled(Button)<{ isMobile: boolean }>`
  width: 100%;
  max-width: 400px;

  align-self: center;

  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body2 : theme.fonts.heading6};
`;
