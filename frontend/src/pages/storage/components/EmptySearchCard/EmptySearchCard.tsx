import styled from '@emotion/styled';
import EmptySearchIcon from '@/components/icons/EmptySearchIcon';

interface EmptySearchCardProps {
  searchQuery: string;
}

function EmptySearchCard({ searchQuery }: EmptySearchCardProps) {
  return (
    <Container>
      <EmptySearchIconWrapper>
        <EmptySearchIcon />
      </EmptySearchIconWrapper>

      <EmptyTitle>검색 결과가 없어요</EmptyTitle>

      <DescriptionWrapper>
        <Lead>{`"${searchQuery}"에 대한 검색 결과를 찾을 수 없습니다.`}</Lead>
        <Support>
          다른 검색어를 시도하거나 카테고리 필터를 조정해보세요.
        </Support>
      </DescriptionWrapper>
    </Container>
  );
}

export default EmptySearchCard;

const Container = styled.section`
  display: flex;
  gap: 22px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  width: 100%;
`;

const EmptySearchIconWrapper = styled.div`
  padding: 38px;
`;

const EmptyTitle = styled.h2`
  background: linear-gradient(90deg, #181818 0%, #f96 100%);
  background-clip: text;

  font: ${({ theme }) => theme.fonts.heading2};
  text-align: center;

  -webkit-text-fill-color: transparent;
`;

const DescriptionWrapper = styled.div`
  display: flex;
  gap: 8px;
  flex-direction: column;
`;

const Lead = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

const Support = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
  text-align: center;
`;
