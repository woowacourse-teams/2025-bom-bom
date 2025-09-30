import styled from '@emotion/styled';

export default function GuideMail2() {
  return (
    <Container>
      <HeaderBox>
        <HeaderTag>봄봄 사용 가이드</HeaderTag>
        <HeaderTitle>키우기</HeaderTitle>
        <HeaderDescription>
          봄이랑 함께하는 읽기 모험! 매일 출석하고 아티클을 읽으면 귀여운 봄이가
          쑥쑥 자라나요.
        </HeaderDescription>
      </HeaderBox>

      <SectionTitle>🌱 키우기 가이드</SectionTitle>
      <BodyText>
        봄이랑 함께하는 읽기 모험!
        <br />
        매일 출석하고 아티클을 읽으면 귀여운 봄이가 쑥쑥 자라나요.
        <br />
        <strong>레벨 5까지 성장</strong>시켜 보세요! 🌱
      </BodyText>
      <Image
        src="/assets/gif/guide_grow.gif"
        alt="가이드 영상"
        style={{ width: '300px' }}
      />

      <SectionTitle>🍀 경험치 모으는 방법</SectionTitle>
      <List>
        <ListItem>
          <strong>🗓 출석 체크</strong>: 하루 한 번, 봄이에게 인사만 해도
          경험치가 쌓여요!
        </ListItem>
        <ListItem>
          <strong>📖 아티클 읽기</strong>: 오늘 도착한 아티클을 읽으면 (최대
          3개) 추가 경험치를 얻을 수 있어요!
        </ListItem>
        <ListItem>
          <strong>🎁 연속 보너스</strong>: 7일 이상 꾸준히 읽으면 보너스
          점수까지 받을 수 있어요!
          <br />
          <SubText>
            (아티클이 안 온 날은 연속이 끊기지 않아요, 안심하세요 💤)
          </SubText>
        </ListItem>
      </List>

      <SectionTitle>📊 내 진행 상황 보기</SectionTitle>
      <BodyText>오른쪽 패널에서 다음 정보를 바로 확인할 수 있어요:</BodyText>
      <List>
        <ListItem>✨ 연속 읽은 일수</ListItem>
        <ListItem>✨ 오늘의 읽기 진행률</ListItem>
        <ListItem>✨ 주간 목표 달성률</ListItem>
      </List>

      <SectionTitle>💡 이렇게 즐겨보세요</SectionTitle>
      <OrderedList>
        <ListItem>1. 오늘의 뉴스레터 ✉️ 확인하기</ListItem>
        <ListItem>2. 아티클 읽고 경험치 챙기기 📚</ListItem>
        <ListItem>3. 봄이가 레벨업하는 모습 구경하기 🐰💕</ListItem>
        <ListItem>4. 주간 목표 달성률 보며 뿌듯해하기 🌟</ListItem>
      </OrderedList>

      <InfoBox>
        <InfoText>
          작은 읽기가 모여 큰 성장이 돼요.
          <br />
          오늘도 봄이랑 같이 시작해볼까요? 🌱
        </InfoText>
      </InfoBox>

      <ButtonWrapper>
        <ActionButton href="/today">출석하고 읽기 시작하기</ActionButton>
      </ButtonWrapper>
    </Container>
  );
}

/* ------------------------------ Styled Components ------------------------------ */

const Container = styled.div`
  max-width: 680px;
  margin: 0 auto;
  padding: 24px 20px;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body1};
  line-height: 1.65;
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
  font-weight: 700;
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

const BodyText = styled.p`
  margin: 0 0 16px;

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

const List = styled.ul`
  margin: 0 0 16px 18px;
  padding: 0;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const OrderedList = styled.ol`
  margin: 0 0 16px 18px;
  padding: 0;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const ListItem = styled.li`
  margin: 8px 0;
`;

const SubText = styled.span`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const InfoBox = styled.div`
  margin: 20px 0;
  padding: 16px;
  border: 1px solid #bae6fd;
  border-radius: 12px;

  background: #f0f9ff;
`;

const InfoText = styled.p`
  margin: 0;

  color: #0284c7;
  font: ${({ theme }) => theme.fonts.body2};
  line-height: 1.6;
`;

const ButtonWrapper = styled.div`
  margin: 24px 0 8px;
  text-align: center;
`;

const ActionButton = styled.a`
  padding: 11px 16px;
  border-radius: 999px;

  display: inline-block;

  background: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.body1};
  font-weight: 700;

  text-decoration: none;

  &:hover {
    opacity: 0.9;
    transition: 0.15s ease-in-out;
  }
`;
