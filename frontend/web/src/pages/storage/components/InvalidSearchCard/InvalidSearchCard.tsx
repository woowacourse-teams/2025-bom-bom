import styled from '@emotion/styled';
import { useDevice } from '@/hooks/useDevice';
import EmptySearchIcon from '#/assets/svg/empty-search.svg';

function InvalidSearchCard() {
  const device = useDevice();
  const isMobile = device === 'mobile';

  return (
    <Container>
      <EmptySearchIconWrapper>
        <EmptySearchIcon width={100} height={100} />
      </EmptySearchIconWrapper>

      <EmptyTitle isMobile={isMobile}>
        {`원하시는 뉴스레터를 찾기 위해 \n 2~100자를 입력해주세요`}
      </EmptyTitle>
    </Container>
  );
}

export default InvalidSearchCard;

const Container = styled.section`
  width: 100%;

  display: flex;
  gap: 22px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const EmptySearchIconWrapper = styled.div`
  padding: 38px;
`;

const EmptyTitle = styled.h2<{ isMobile: boolean }>`
  background: linear-gradient(90deg, #181818 0%, #f96 100%);
  background-clip: text;
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.heading4 : theme.fonts.heading2};
  text-align: center;
  white-space: pre-wrap;

  -webkit-text-fill-color: transparent;
`;
