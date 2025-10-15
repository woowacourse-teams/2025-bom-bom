import styled from '@emotion/styled';

const NewsletterSubscribeGuide = () => {
  return (
    <Container>
      <SubscribeHeader>
        <SubscribeTitle>구독 방법</SubscribeTitle>
      </SubscribeHeader>

      <SubscribeContent>
        <StepsWrapper>
          <StepItem>
            <StepNumber>1</StepNumber>
            <StepContent>
              <StepTitle>구독하기 버튼 클릭</StepTitle>
              <StepDescription>
                {'위의 "구독하기" 버튼을 눌러주세요.'}
              </StepDescription>
            </StepContent>
          </StepItem>
          <StepItem>
            <StepNumber>2</StepNumber>
            <StepContent>
              <StepTitle>구독 페이지 접속</StepTitle>
              <StepDescription>
                뉴스레터 공식 구독 페이지로 이동합니다.
              </StepDescription>
            </StepContent>
          </StepItem>
          <StepItem>
            <StepNumber>3</StepNumber>
            <StepContent>
              <StepTitle>봄봄 메일 붙여넣기</StepTitle>
              <StepDescription>
                이메일 칸에 봄봄 메일을 입력해주세요.
              </StepDescription>
              <StepDescription>
                봄봄을 통해 접속한 유저라면 즉시 붙여넣기가 가능합니다!
              </StepDescription>
            </StepContent>
          </StepItem>
          <StepItem>
            <StepNumber>4</StepNumber>
            <StepContent>
              <StepTitle>구독 완료!</StepTitle>
              <StepDescription>
                축하합니다! 이제 정기적으로 뉴스레터를 받아보세요.
              </StepDescription>
            </StepContent>
          </StepItem>
        </StepsWrapper>
      </SubscribeContent>
    </Container>
  );
};

export default NewsletterSubscribeGuide;

const Container = styled.div`
  padding: 8px 16px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 8px;

  display: flex;
  gap: 8px;
  flex-direction: column;
  align-items: center;

  background: ${({ theme }) => theme.colors.white};
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const SubscribeHeader = styled.div`
  width: 100%;
  padding: 8px 0;
`;

const SubscribeTitle = styled.h3`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading6};
`;

const SubscribeContent = styled.div`
  width: 100%;
  padding: 16px;
  border-top: 1px solid ${({ theme }) => theme.colors.dividers};
`;

const StepsWrapper = styled.div`
  position: relative;
  padding: 20px 16px;

  &::before {
    position: absolute;
    top: 36px;
    bottom: 68px;
    left: 32px;
    width: 2px;

    background: ${({ theme }) => theme.colors.dividers};

    content: '';
  }
`;

const StepItem = styled.div`
  position: relative;
  margin-bottom: 32px;

  display: flex;
  gap: 16px;
  align-items: flex-start;

  &:last-child {
    margin-bottom: 0;
  }
`;

const StepNumber = styled.span`
  z-index: ${({ theme }) => theme.zIndex.content};
  width: 32px;
  height: 32px;
  border: 2px solid ${({ theme }) => theme.colors.white};
  border-radius: 50%;
  box-shadow: 0 2px 8px rgb(0 0 0 / 10%);

  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  background: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.body2};
`;

const StepContent = styled.div`
  display: flex;
  gap: 4px;
  flex-direction: column;
`;

const StepTitle = styled.p`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const StepDescription = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;
