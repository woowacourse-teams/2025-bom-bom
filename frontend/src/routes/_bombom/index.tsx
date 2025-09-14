import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { useMemo } from 'react';
import ArticleCardList from '../../pages/today/components/ArticleCardList/ArticleCardList';
import ReadingStatusCard from '../../pages/today/components/ReadingStatusCard/ReadingStatusCard';
import { queries } from '@/apis/queries';
import PetCard from '@/components/PetCard/PetCard';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import { theme } from '@/styles/theme';
import { LocalGuideMail } from '@/types/guide';
import { isToday } from '@/utils/date';
import { createStorage } from '@/utils/localStorage';
import type { CSSObject, Theme } from '@emotion/react';
import HomeIcon from '#/assets/home.svg';

export const Route = createFileRoute('/_bombom/')({
  head: () => ({
    meta: [
      {
        name: 'robots',
        content: 'noindex, nofollow',
      },
    ],
  }),
  component: Index,
});

function Index() {
  const today = useMemo(() => new Date(), []);
  const { data: todayArticles } = useQuery(queries.articles({ date: today }));
  const guideArticles = createStorage<LocalGuideMail[], string>(
    'guide-mail',
  ).get();

  const mergedArticles = [
    ...(todayArticles?.content?.map((article) => ({
      ...article,
      type: 'article' as const,
    })) ?? []),
    ...(guideArticles
      ?.filter((guide) => isToday(new Date(guide.createdAt)))
      .map((guide) => ({ ...guide, type: 'guide' as const })) ?? []),
  ];

  const deviceType = useDeviceType();

  return (
    <Container deviceType={deviceType}>
      {deviceType !== 'mobile' && (
        <>
          <TitleWrapper>
            <TitleIconBox>
              <HomeIcon width={20} height={20} color={theme.colors.white} />
            </TitleIconBox>
            <Title>오늘의 뉴스레터</Title>
          </TitleWrapper>
        </>
      )}

      <ContentWrapper deviceType={deviceType}>
        <ArticleCardList articles={mergedArticles} />
        <ReaderCompanion deviceType={deviceType}>
          <PetCard />
          <ReadingStatusCard />
        </ReaderCompanion>
      </ContentWrapper>
    </Container>
  );
}

const Container = styled.div<{ deviceType: DeviceType }>`
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;

  display: flex;
  gap: 24px;
  flex-direction: column;
  align-items: flex-start;

  box-sizing: border-box;
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
`;

const TitleIconBox = styled.div`
  width: 28px;
  height: 28px;
  border-radius: 50%;

  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const Title = styled.h1`
  font: ${({ theme }) => theme.fonts.heading3};
`;

const ContentWrapper = styled.div<{ deviceType: DeviceType }>`
  width: 100%;

  display: flex;
  gap: 24px;
  flex-direction: ${({ deviceType }) =>
    deviceType === 'pc' ? 'row' : 'column-reverse'};
  align-items: ${({ deviceType }) =>
    deviceType === 'pc' ? 'flex-start' : 'center'};
  align-self: stretch;
  justify-content: center;
`;

const ReaderCompanion = styled.div<{ deviceType: DeviceType }>`
  min-width: 300px;

  display: flex;
  gap: 36px;

  box-sizing: border-box;

  ${({ deviceType, theme }) => sideCardWrapperStyles[deviceType](theme)}
`;

const sideCardWrapperStyles: Record<DeviceType, (theme: Theme) => CSSObject> = {
  pc: (theme) => ({
    width: '310px',
    flexDirection: 'column',
    justifyContent: 'flex-start',
    alignItems: 'center',
    gap: '24px',
    border: `1px solid ${theme.colors.white}`,
  }),
  tablet: () => ({
    width: '100%',
    maxWidth: 'calc(100% - 200px)',
    justifyContent: 'center',
    alignItems: 'flex-start',
  }),
  mobile: () => ({
    position: 'relative',
    paddingBottom: '44px',
    width: '100%',
    justifyContent: 'center',
    alignItems: 'center',
  }),
};
