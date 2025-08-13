import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { useMemo } from 'react';
import ArticleCardList from '../../pages/today/components/ArticleCardList/ArticleCardList';
import ReadingStatusCard from '../../pages/today/components/ReadingStatusCard/ReadingStatusCard';
import { queries } from '@/apis/queries';
import PetCard from '@/components/PetCard/PetCard';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import type { CSSObject, Theme } from '@emotion/react';

export const Route = createFileRoute('/_bombom/')({
  component: Index,
});

function Index() {
  const today = useMemo(() => new Date(), []);
  const { data: todayArticles } = useQuery(queries.articles({ date: today }));
  const device = useDeviceType();

  return (
    <Container device={device}>
      <TitleBox>
        <Title>오늘의 뉴스레터</Title>
        <TitleDescription>
          {todayArticles?.content?.length ?? 0}개의 새로운 뉴스레터가 도착했어요
        </TitleDescription>
      </TitleBox>
      <ContentWrapper device={device}>
        <ArticleCardList articles={todayArticles?.content ?? []} />
        <SideCardWrapper device={device}>
          <PetCard />
          <ReadingStatusCard />
        </SideCardWrapper>
      </ContentWrapper>
    </Container>
  );
}

const Container = styled.div<{ device: DeviceType }>`
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  padding-top: ${({ device }) => (device === 'pc' ? '64px' : '32px')};

  display: flex;
  gap: 24px;
  flex-direction: column;
  align-items: flex-start;

  box-sizing: border-box;
`;

const TitleBox = styled.div`
  display: flex;
  gap: 8px;
  flex-direction: column;
  align-items: flex-start;
`;

const Title = styled.h1`
  font: ${({ theme }) => theme.fonts.heading2};
`;

const TitleDescription = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const ContentWrapper = styled.div<{ device: DeviceType }>`
  width: 100%;

  display: flex;
  gap: 24px;
  flex-direction: ${({ device }) =>
    device === 'pc' ? 'row' : 'column-reverse'};
  align-items: ${({ device }) => (device === 'pc' ? 'flex-start' : 'center')};
  align-self: stretch;
  justify-content: center;
`;

const SideCardWrapper = styled.div<{ device: DeviceType }>`
  display: flex;
  justify-content: flex-start;

  ${({ device, theme }) => sideCardWrapperStyles[device](theme)}
`;

const sideCardWrapperStyles: Record<DeviceType, (theme: Theme) => CSSObject> = {
  pc: (theme) => ({
    width: '310px',
    border: `1px solid ${theme.colors.white}`,
    gap: '24px',
    flexDirection: 'column',
    alignItems: 'center',
  }),
  tablet: () => ({
    width: '100%',
    borderRadius: '20px',
    boxShadow: '0 25px 50px -12px rgb(0 0 0 / 15%)',
    alignItems: 'flex-start',
  }),
  mobile: () => ({
    width: '100%',
    borderRadius: '20px',
    boxShadow: '0 25px 50px -12px rgb(0 0 0 / 15%)',
    alignItems: 'flex-start',
  }),
};
