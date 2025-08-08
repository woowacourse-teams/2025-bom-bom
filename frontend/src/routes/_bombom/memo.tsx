import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import MemoCard from '@/pages/detail/components/MemoCard/MemoCard';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';
import { theme } from '@/styles/theme';
import MemoIcon from '#/assets/memo.svg';

export const Route = createFileRoute('/_bombom/memo')({
  component: MemoPage,
});

function MemoPage() {
  const navigate = useNavigate();
  const { data: highlights } = useQuery(queries.highlights({}));

  return (
    <Container>
      <TitleWrapper>
        <BookmarkStorageIcon fill={theme.colors.white} />
        <Title>메모 보관함</Title>
      </TitleWrapper>
      {highlights && highlights.length > 0 ? (
        <MemoList>
          {highlights.map((highlight) => (
            <li key={highlight.articleId}>
              <MemoCard
                id={highlight.id}
                content={highlight.text}
                memo={highlight.memo}
                as="button"
                onClick={() =>
                  navigate({ to: `/articles/${highlight.articleId}` })
                }
              />
            </li>
          ))}
        </MemoList>
      ) : (
        <EmptyLetterCard title="메모한 뉴스레터가 없어요" />
      )}
    </Container>
  );
}

export default MemoPage;

const Container = styled.div`
  width: 100%;
  max-width: 1280px;
  padding: 64px 0;

  display: flex;
  gap: 24px;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const Title = styled.h1`
  font: ${({ theme }) => theme.fonts.heading2};
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
