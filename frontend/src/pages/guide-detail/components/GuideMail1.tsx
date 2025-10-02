import styled from '@emotion/styled';
import { Link } from '@tanstack/react-router';

export default function GuideMail1() {
  return (
    <Container>
      <HeaderBox>
        <HeaderTag>봄봄 사용 가이드</HeaderTag>
        <HeaderTitle>뉴스레터 구독하기</HeaderTitle>
        <HeaderDescription>
          관심 뉴스레터를 선택하고 구독해서 <strong>봄봄</strong>에서
          받아보세요.
        </HeaderDescription>
      </HeaderBox>

      <SectionTitle>🔍 뉴스레터 둘러보기</SectionTitle>
      <TipBox>
        <TipText>
          상단 네비게이션에서 <strong>「뉴스레터 추천」</strong> 페이지로
          이동해보세요.
          <br />
          관심 카테고리를 선택하고 발행 사이트 리스트를 확인할 수 있어요.
          <br />
          원하는 사이트를 클릭해서 상세 소개를 확인해보세요.
        </TipText>
      </TipBox>

      <WarningBox>
        <WarningTitle>⚠️ 주의사항</WarningTitle>
        <WarningDescription>
          각 뉴스레터의 발행 요일을 꼭 확인해주세요.
        </WarningDescription>
      </WarningBox>

      <SectionTitle>📧 구독 페이지 접속하기</SectionTitle>
      <BodyText>
        원하는 뉴스레터에서 <strong>「구독하기」</strong> 버튼을 눌러 구독
        페이지로 이동해보세요.
      </BodyText>
      <Image src="/assets/gif/guide_subscribe_1.gif" alt="뉴스레터 구독 - 1" />

      <WarningBox>
        <WarningTitle>⚠️ 주의사항</WarningTitle>
        <WarningDescription>
          버튼을 누르면 봄봄 메일이 자동으로 복사돼요.
        </WarningDescription>
      </WarningBox>

      <SectionTitle>📝 봄봄 메일 붙여넣기</SectionTitle>
      <BodyText>
        구독 페이지의 이메일 입력 칸에 복사된 봄봄 메일을 붙여넣기 해주세요.
        <br />
        봄봄 계정으로 접속 중이라면 자동으로 붙여넣기가 가능해요.
      </BodyText>
      <Image src="/assets/gif/guide_subscribe_2.gif" alt="뉴스레터 구독 - 2" />

      <SectionTitle>✅ 구독 완료</SectionTitle>
      <BodyText>
        🎉 축하해요! 이제 정기적으로 뉴스레터를 받아보실 수 있어요.
      </BodyText>
      <Image src="/assets/gif/guide_subscribe_3.gif" alt="뉴스레터 구독 - 3" />

      <WarningBox>
        <WarningTitle>⚠️ 주의사항</WarningTitle>
        <WarningDescription>
          일부 뉴스레터는 환영 메일을 따로 발송하지 않을 수 있어요.
          <br />
          구독 후에는 <strong>「오늘의 뉴스레터」</strong>에서 도착한 아티클을
          깔끔하게 확인할 수 있어요.
        </WarningDescription>
      </WarningBox>

      <ButtonWrapper>
        <SubscribeLink to="/">지금 구독하러 가기</SubscribeLink>
      </ButtonWrapper>
    </Container>
  );
}

const Container = styled.div`
  max-width: 680px;
  margin: 0 auto;
  padding: 24px 0;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const HeaderBox = styled.div`
  margin-bottom: 20px;
  padding: 20px 18px;
  border: 1px solid #ffedd5;
  border-radius: 16px;

  background: #fff7ed;
`;

const HeaderTag = styled.div`
  margin-bottom: 6px;

  color: ${({ theme }) => theme.colors.primary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const HeaderTitle = styled.h1`
  margin: 0 0 10px;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading4};
`;

const HeaderDescription = styled.p`
  margin: 0;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const SectionTitle = styled.h2`
  margin: 24px 0 12px;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const TipBox = styled.div`
  margin-bottom: 16px;
  padding-left: 16px;
  border-left: 4px solid ${({ theme }) => theme.colors.primary};
`;

const TipText = styled.p`
  margin: 0 0 8px;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const WarningBox = styled.div`
  margin-bottom: 16px;
  padding: 12px;
  border: 1px solid #fbbf24;
  border-radius: 8px;

  background: #fef3c7;
`;

const WarningTitle = styled.strong`
  display: block;

  color: #92400e;
  font: ${({ theme }) => theme.fonts.body2};
`;

const WarningDescription = styled.span`
  color: #451a03;
  font: ${({ theme }) => theme.fonts.body1};
`;

const BodyText = styled.p`
  margin: 0 0 12px;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const Image = styled.img`
  width: 640px;
  max-width: 100%;
  margin: 8px 0 16px;
  border-radius: 4px;

  display: block;
`;

const ButtonWrapper = styled.div`
  margin: 24px 0 8px;
  text-align: center;
`;

const SubscribeLink = styled(Link)`
  padding: 11px 16px;
  border-radius: 999px;

  display: inline-block;

  background: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.body1};

  &:hover {
    opacity: 0.9;
    transition: 0.15s ease-in-out;
  }
`;
