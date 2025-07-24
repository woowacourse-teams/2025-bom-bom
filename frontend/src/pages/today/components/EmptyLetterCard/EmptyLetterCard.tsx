import styled from '@emotion/styled';
import { Link } from '@tanstack/react-router';
import ArrowIcon from '@/components/icons/ArrowIcon';
import CompassIcon from '@/components/icons/CompassIcon';
import PostboxIcon from '@/components/icons/PostboxIcon';

interface EmptyLetterCardProps {
  title: string;
}

function EmptyLetterCard({ title }: EmptyLetterCardProps) {
  return (
    <Container>
      <PostboxIconWrapper>
        <PostboxIcon />
      </PostboxIconWrapper>

      <EmptyTitle>{title}</EmptyTitle>

      <Description>
        <Lead>뉴스레터를 구독하고 봄봄에서 편리하게 관리해보세요.</Lead>
        <Support>
          구독한 뉴스레터들이 여기에 깔끔하게 정리되어 나타납니다.
        </Support>
      </Description>

      <LinkButton to="/recommend">
        <CompassIcon />
        추천 뉴스레터 보기
        <StyledArrowIcon direction="right" />
      </LinkButton>
    </Container>
  );
}

export default EmptyLetterCard;

const Container = styled.section`
  display: flex;
  gap: 22px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  width: 100%;
`;

const PostboxIconWrapper = styled.div`
  padding: 46px;
`;

const EmptyTitle = styled.p`
  background: linear-gradient(90deg, #181818 0%, #f96 100%);
  background-clip: text;

  font: ${({ theme }) => theme.fonts.heading2};
  text-align: center;

  -webkit-text-fill-color: transparent;
`;

const Description = styled.div`
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

const LinkButton = styled(Link)`
  display: flex;
  gap: 4px;
  align-items: center;
  justify-content: center;

  padding: 10px 12px;
  border-radius: 12px;

  background: ${({ theme }) => theme.colors.primary};

  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.body1};

  transition: all 0.2s ease;

  &:hover {
    filter: brightness(0.9);
  }
`;

const StyledArrowIcon = styled(ArrowIcon)`
  width: 18px;
  height: 18px;
`;
