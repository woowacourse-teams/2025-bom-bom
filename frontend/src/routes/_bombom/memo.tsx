import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { useState, useCallback } from 'react';
import { queries } from '@/apis/queries';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import MemoCard from '@/pages/detail/components/MemoCard/MemoCard';
import NewsLetterFilter from '@/pages/storage/components/NewsletterFilter/NewsletterFilter';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';
import { theme } from '@/styles/theme';
import MemoIcon from '#/assets/memo.svg';

export const Route = createFileRoute('/_bombom/memo')({
  component: MemoPage,
});

function MemoPage() {
  const navigate = useNavigate();
  const { data: highlights } = useQuery(queries.highlights());
  const [selectedNewsletter, setSelectedNewsletter] = useState('전체');
  const deviceType = useDeviceType();

  const handleNewsletterChange = useCallback((value: string) => {
    setSelectedNewsletter(value);
  }, []);

  const { data: newletterCounts } = useQuery(
    queries.highlightStatisticsNewsletter(),
  );

  console.log(newletterCounts);

  if (!highlights) return null;

  return (
    <Container>
      <MainSection>
        <TitleWrapper>
          <BookmarkStorageIcon fill={theme.colors.white} />
          <Title>메모 보관함</Title>
        </TitleWrapper>

        <ContentWrapper deviceType={deviceType}>
          <SidebarSection deviceType={deviceType}>
            <NewsLetterFilter
              newsLetterList={[
                {
                  newsletter: '전체',
                  count: newletterCounts?.totalCount ?? 0,
                  imageUrl: '',
                },
                ...(newletterCounts?.newsletters
                  .filter((newsletter) => newsletter.highlightCount > 0)
                  .map((newsletter) => ({
                    newsletter: newsletter.name,
                    count: newsletter.highlightCount,
                    imageUrl: newsletter.imageUrl,
                  })) ?? []),
              ]}
              selectedNewsletter={selectedNewsletter}
              onSelectNewsletter={handleNewsletterChange}
            />
          </SidebarSection>

          <MainContentSection deviceType={deviceType}>
            {highlights.content && highlights.content.length > 0 ? (
              <MemoList>
                {highlights.content.map((highlight) => (
                  <li key={highlight.articleId}>
                    <MemoCard
                      id={highlight.id}
                      content={highlight.text}
                      memo={highlight.memo}
                      as="button"
                      onClick={() =>
                        navigate({ to: `/articles/${highlight.articleId}` })
                      }
                      newsletterName="뉴닉"
                      newsletterImageUrl="https://newneek.co/favicon.ico"
                      articleTitle="뉴스레터 제목"
                      createdAt="2025-01-01"
                    />
                  </li>
                ))}
              </MemoList>
            ) : (
              <EmptyLetterCard title="메모한 뉴스레터가 없어요" />
            )}
          </MainContentSection>
        </ContentWrapper>
      </MainSection>
    </Container>
  );
}

export default MemoPage;

const Container = styled.div`
  width: 100%;
  max-width: 1280px;
  padding: 64px 0;

  display: flex;
  align-items: flex-start;
  justify-content: center;
`;

const MainSection = styled.div`
  width: 100%;

  display: flex;
  gap: 20px;
  flex-direction: column;
  align-items: flex-start;
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const Title = styled.h1`
  font: ${({ theme }) => theme.fonts.heading2};
`;

const ContentWrapper = styled.div<{ deviceType: DeviceType }>`
  width: 100%;

  display: flex;
  gap: ${({ deviceType }) => (deviceType === 'pc' ? '32px' : '20px')};
  flex-direction: ${({ deviceType }) =>
    deviceType === 'pc' ? 'row' : 'column'};
  align-items: flex-start;
`;

const SidebarSection = styled.div<{ deviceType: DeviceType }>`
  width: ${({ deviceType }) => (deviceType === 'pc' ? '320px' : '100%')};

  display: flex;
  gap: 20px;
  flex-direction: column;

  order: ${({ deviceType }) => (deviceType === 'pc' ? 1 : 0)};
`;

const MainContentSection = styled.div<{ deviceType: DeviceType }>`
  display: flex;
  gap: 20px;
  flex: 1;
  flex-direction: column;

  order: ${({ deviceType }) => (deviceType === 'pc' ? 2 : 1)};
`;

const BookmarkStorageIcon = styled(MemoIcon)`
  width: 36px;
  height: 36px;
  padding: 8px;
  border-radius: 50%;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  text-align: center;
`;

const MemoList = styled.ul`
  width: 100%;

  display: grid;
  gap: 16px;

  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
`;
