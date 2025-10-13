import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { buildSubscribeUrl, isMaily, isStibee } from './NewsletterDetail.utils';
import NewsletterSubscribeGuide from './NewsletterSubscribeGuide';
import PreviousArticleListItem from './PreviousArticleListItem';
import { queries } from '@/apis/queries';
import Badge from '@/components/Badge/Badge';
import Button from '@/components/Button/Button';
import ImageWithFallback from '@/components/ImageWithFallback/ImageWithFallback';
import { useDevice } from '@/hooks/useDevice';
import { useUserInfo } from '@/hooks/useUserInfo';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import { copyToClipboard } from '@/utils/copy';
import { openExternalLink } from '@/utils/externalLink';
import ArticleHistoryIcon from '#/assets/svg/article-history.svg';
import HomeIcon from '#/assets/svg/home.svg';

interface NewsletterDetailProps {
  newsletterId: number;
  category: string;
}

const NewsletterDetail = ({
  newsletterId,
  category,
}: NewsletterDetailProps) => {
  const { userInfo, isLoggedIn } = useUserInfo();
  const { data: newsletterDetail } = useQuery({
    ...queries.newsletterDetail({ id: newsletterId }),
    enabled: Boolean(newsletterId),
  });
  const { data: previousArticles } = useQuery({
    ...queries.previousArticles({ newsletterId, limit: 10 }),
  });
  const deviceType = useDevice();
  const [activeTab, setActiveTab] = useState<'detail' | 'previous'>('detail');

  const isMobile = deviceType === 'mobile';

  if (!newsletterId || !newsletterDetail) return null;

  const openSubscribe = () => {
    if (!isLoggedIn || !userInfo) return;

    if (
      !isStibee(newsletterDetail.subscribeUrl) &&
      !isMaily(newsletterDetail.subscribeUrl)
    ) {
      copyToClipboard(userInfo.email);
      alert('이메일이 복사되었습니다. 이 이메일로 뉴스레터를 구독해주세요.');
    }

    trackEvent({
      category: 'Newsletter',
      action: '구독하기 버튼 클릭',
      label: newsletterDetail.name ?? 'Unknown Newsletter',
    });

    const subscribeUrl = buildSubscribeUrl(
      newsletterDetail.subscribeUrl,
      userInfo,
    );

    openExternalLink(subscribeUrl);
  };

  const openMainSite = () => {
    openExternalLink(newsletterDetail.mainPageUrl);
  };

  const openPreviousLetters = () => {
    if (!newsletterDetail.previousNewsletterUrl) return;
    openExternalLink(newsletterDetail.previousNewsletterUrl);
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
              <StyledBadge text={category} isMobile={isMobile} />
              <IssueCycle>{`${newsletterDetail.issueCycle} 발행`}</IssueCycle>
            </NewsletterInfo>
          </InfoBox>
        </InfoWrapper>

        <SubscribeButton
          text={isLoggedIn ? '구독하기' : '로그인 후 구독할 수 있어요'}
          onClick={openSubscribe}
          disabled={!isLoggedIn}
          isMobile={isMobile}
        />
      </FixedWrapper>

      <TabHeader>
        <TabButton
          isActive={activeTab === 'detail'}
          onClick={() => setActiveTab('detail')}
        >
          뉴스레터 소개
        </TabButton>
        <TabButton
          isActive={activeTab === 'previous'}
          onClick={() => setActiveTab('previous')}
        >
          지난 뉴스레터
        </TabButton>
      </TabHeader>

      {activeTab === 'detail' && (
        <ScrollableWrapper isMobile={isMobile}>
          <Description isMobile={isMobile}>
            {newsletterDetail.description}
          </Description>

          {newsletterDetail.previousNewsletterUrl && (
            <DetailLink onClick={openPreviousLetters} isMobile={isMobile}>
              <ArticleHistoryIcon width={16} height={16} />
              지난 소식 보기
            </DetailLink>
          )}

          {!isMobile && <NewsletterSubscribeGuide />}
        </ScrollableWrapper>
      )}

      {activeTab === 'previous' && (
        <ScrollableWrapper isMobile={isMobile}>
          <PreviousArticleList>
            {previousArticles?.map((article) => (
              <PreviousArticleListItem
                key={article.articleId}
                {...article}
                onClick={(id) => console.log('이전 아티클 클릭', id)}
              />
            ))}
          </PreviousArticleList>
        </ScrollableWrapper>
      )}
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

  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '16px' : '24px')};
  flex-direction: column;

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

const Description = styled.p<{ isMobile: boolean }>`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ isMobile, theme }) =>
    isMobile ? theme.fonts.body2 : theme.fonts.body1};
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

  transition: all 0.2s ease;

  &:hover {
    filter: brightness(90%);
  }
`;

const TabHeader = styled.div`
  display: flex;
  border-bottom: 1px solid ${({ theme }) => theme.colors.dividers};
`;

const TabButton = styled.button<{ isActive: boolean }>`
  flex: 1;
  padding: 12px 0;
  font: ${({ theme }) => theme.fonts.body2};
  color: ${({ theme, isActive }) =>
    isActive ? theme.colors.primary : theme.colors.textSecondary};
  border-bottom: 2px solid
    ${({ theme, isActive }) =>
      isActive ? theme.colors.primary : 'transparent'};
  background: none;
  transition: color 0.2s;

  &:hover {
    color: ${({ theme }) => theme.colors.primary};
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
`;

const PreviousArticleList = styled.div``;
