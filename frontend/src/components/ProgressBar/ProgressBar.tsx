import styled from '@emotion/styled';

type VariantType = 'rounded' | 'rectangular';

interface ProgressBarProps {
  rate: number;
  caption?: string;
  transition?: boolean | number;
  variant?: VariantType;
}

/**
 * 진행률을 시각적으로 표시하는 컴포넌트입니다.
 *
 * @param rate - 진행률(%) (0-100)
 * @param caption - 진행률 설명 텍스트 (선택사항)
 * @param transition - 애니메이션 전환 시간 (false: 애니메이션 비활성화, true: 기본값, number: 해당 시간(초), 기본값: 0.5)
 * @param variant - 진행바 스타일 ('rounded' | 'rectangular', 기본값: 'rounded')
 */
const ProgressBar = ({
  rate,
  caption,
  transition = 0.5,
  variant = 'rounded',
  ...props
}: ProgressBarProps) => {
  return (
    <Container {...props}>
      <ProgressOverlay variant={variant}>
        <ProgressGauge
          rate={rate}
          transitionDuration={transition === true ? 0.5 : transition}
          variant={variant}
        />
      </ProgressOverlay>
      <ProgressCaption>{caption}</ProgressCaption>
    </Container>
  );
};

export default ProgressBar;

const Container = styled.div`
  width: 100%;
`;

const ProgressOverlay = styled.div<{ variant: 'rounded' | 'rectangular' }>`
  width: 100%;
  height: 10px;
  border-radius: ${({ variant }) => (variant === 'rounded' ? '10px' : '0')};

  background-color: ${({ theme }) => theme.colors.primaryLight};
`;

const ProgressGauge = styled.div<{
  rate: number;
  transitionDuration: false | number;
  variant: 'rounded' | 'rectangular';
}>`
  overflow: hidden;
  width: ${({ rate }) => `${rate}%`};
  height: 100%;
  max-width: 100%;
  border-radius: ${({ rate, variant }) => {
    if (variant === 'rectangular') return '0';
    return rate >= 100 ? '10px' : '10px 0 0 10px';
  }};

  background-color: ${({ theme }) => theme.colors.primary};

  ${({ transitionDuration }) =>
    transitionDuration &&
    `transition: width ${transitionDuration}s ease-in-out`};
`;

const ProgressCaption = styled.p`
  width: 100%;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.caption};
  text-align: end;
`;
