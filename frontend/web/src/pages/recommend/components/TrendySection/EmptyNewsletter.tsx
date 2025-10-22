import styled from '@emotion/styled';
import EmptySearchIcon from '#/assets/svg/empty-search.svg';

const EmptyNewsletter = () => {
  return (
    <Container>
      <EmptySearchIconWrapper>
        <EmptySearchIcon width={80} height={80} />
      </EmptySearchIconWrapper>

      <EmptyTitle>검색 결과가 없어요</EmptyTitle>

      <DescriptionWrapper>
        <Lead>해당 조건에 맞는 뉴스레터를 찾을 수 없습니다.</Lead>
        <Support>다른 검색어나 카테고리를 시도해보세요.</Support>
      </DescriptionWrapper>
    </Container>
  );
};

export default EmptyNewsletter;

const Container = styled.div`
  width: 100%;
  height: 100%;
  padding: 60px 24px;

  display: flex;
  gap: 20px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const EmptySearchIconWrapper = styled.div`
  padding: 20px;
`;

const EmptyTitle = styled.h2`
  margin: 0;

  background: linear-gradient(90deg, #181818 0%, #f96 100%);
  background-clip: text;
  font: ${({ theme }) => theme.fonts.heading4};
  text-align: center;

  -webkit-text-fill-color: transparent;
`;

const DescriptionWrapper = styled.div`
  display: flex;
  gap: 8px;
  flex-direction: column;
`;

const Lead = styled.p`
  margin: 0;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};
  text-align: center;
`;

const Support = styled.p`
  margin: 0;

  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
  text-align: center;
`;
