import styled from '@emotion/styled';

export default function GuideMail3() {
  return (
    <Container>
      <HeaderBox>
        <HeaderTag>봄봄 사용 가이드</HeaderTag>
        <HeaderTitle>하이라이트 & 메모 사용법</HeaderTitle>
        <HeaderDescription>
          읽기 중 중요한 구절을 <strong>하이라이트</strong>하고{' '}
          <strong>개인 메모</strong>를 남겨서 지식을 체계적으로 정리해보세요.
        </HeaderDescription>
      </HeaderBox>

      <SectionTitle>🎯 중요한 문장만 쏙! 하이라이트 & 메모</SectionTitle>
      <BodyText>
        읽기 중 중요한 구절을 <strong>하이라이트</strong>하고{' '}
        <strong>개인 메모</strong>를 남겨서 지식을 체계적으로 정리해보세요.
      </BodyText>

      <SectionTitle>🖍️ 하이라이트 만들기</SectionTitle>
      <HighlightBox>
        <BodyTextSub>
          아티클에서 중요한 구절을 마우스로 드래그해서 선택하면 퀵 메뉴가
          나타나요.
          <br />
          <strong>하이라이트</strong> 버튼을 클릭하면 선택한 텍스트가 노란색으로
          강조 표시돼요.
        </BodyTextSub>

        <Center>
          <Video
            autoPlay
            loop
            muted
            playsInline
            src="/assets/mp4/guide_highlight.mp4"
          />
        </Center>

        <SuccessBox>
          <SuccessIcon>✅</SuccessIcon>
          <SuccessText>
            <strong>완료!</strong> 이제 중요한 구절이 하이라이트되었어요.
          </SuccessText>
        </SuccessBox>
      </HighlightBox>

      <SectionTitle>📝 메모 추가하기</SectionTitle>
      <HighlightBox>
        <BodyTextSub>
          텍스트를 선택한 후 퀵 메뉴에서 <strong>메모</strong> 버튼을 클릭하면
          메모 입력창이 나타나요.
          <br />
          해당 구절에 대한 생각, 질문, 아이디어를 자유롭게 작성해보세요.
        </BodyTextSub>

        <Center>
          <Video
            autoPlay
            loop
            muted
            playsInline
            src="/assets/mp4/guide_memo.mp4"
          />
        </Center>
      </HighlightBox>

      <SectionTitle>📚 내 하이라이트 & 메모 모아보기</SectionTitle>
      <TipBox>
        <TipText>
          <strong>경로</strong>: 좌측 바로가기에서 한 번에 확인할 수 있어요.
        </TipText>
      </TipBox>

      <List>
        <ListItem>
          <strong>🖍️ 하이라이트</strong>: 중요 구절만 모아본 목록을 확인할 수
          있어요
        </ListItem>
        <ListItem>
          <strong>📝 메모</strong>: 내가 남긴 메모가 포함된 하이라이트 목록을 볼
          수 있어요
        </ListItem>
      </List>

      <DashedBox>
        <Video
          autoPlay
          loop
          muted
          playsInline
          src="/assets/mp4/guide_quick_menu.mp4"
        />
      </DashedBox>

      <InfoBox>
        <InfoTitle>🎓 학습 효과 UP!</InfoTitle>
        <InfoText>
          하이라이트와 메모를 활용하면 단순 읽기를 넘어{' '}
          <strong>능동적 학습</strong>이 가능해요.
          <br />
          나만의 지식 아카이브를 만들어서 언제든 다시 찾아볼 수 있어요!
        </InfoText>
      </InfoBox>
    </Container>
  );
}

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

const BodyTextSub = styled.p`
  margin: 0 0 16px;

  color: #475569;
  font: ${({ theme }) => theme.fonts.body2};
`;

const HighlightBox = styled.div`
  margin-bottom: 20px;
  padding: 20px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;

  background: #f8fafc;
`;

const SuccessBox = styled.div`
  padding: 12px;
  border: 1px solid #bbf7d0;
  border-radius: 8px;

  display: flex;
  align-items: center;

  background: #ecfdf5;
`;

const SuccessIcon = styled.span`
  color: #059669;
  font-size: 16px;
`;

const SuccessText = styled.span`
  margin-left: 8px;

  color: #065f46;
  font: ${({ theme }) => theme.fonts.body2};
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
  font-weight: 600;
`;

const List = styled.ul`
  margin: 0 0 20px 18px;
  padding: 0;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const ListItem = styled.li`
  margin: 8px 0;
`;

const DashedBox = styled.div`
  margin-bottom: 20px;
  padding: 12px;
  border: 2px dashed #cbd5e1;
  border-radius: 8px;

  background: #f1f5f9;
  color: #64748b;
  font: ${({ theme }) => theme.fonts.body2};
  font-style: italic;
  text-align: center;
`;

const InfoBox = styled.div`
  margin: 20px 0;
  padding: 16px;
  border: 1px solid #bae6fd;
  border-radius: 12px;

  background: #f0f9ff;
`;

const InfoTitle = styled.div`
  margin-bottom: 8px;

  color: #0369a1;
  font: ${({ theme }) => theme.fonts.heading6};
  font-weight: 600;
`;

const InfoText = styled.p`
  margin: 0;

  color: #0284c7;
  font: ${({ theme }) => theme.fonts.body2};
  line-height: 1.6;
`;

const Video = styled.video`
  width: 100%;
  height: auto;
  max-width: 100%;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
`;

const Center = styled.div`
  margin-bottom: 12px;
  text-align: center;
`;
