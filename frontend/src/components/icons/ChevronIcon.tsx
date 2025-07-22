import styled from '@emotion/styled';
import { SVGProps } from 'react';

interface ChevronIconProps extends SVGProps<SVGSVGElement> {
  direction: 'up' | 'down' | 'left' | 'right';
}

const directionRotationMap = {
  up: 'rotate(180deg)',
  down: 'rotate(0deg)',
  left: 'rotate(90deg)',
  right: 'rotate(-90deg)',
};

function ChevronIcon({ direction, ...props }: ChevronIconProps) {
  return (
    <StyledSVG
      width="16"
      height="16"
      viewBox="0 0 16 16"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      direction={direction}
      {...props}
    >
      <path
        d="M4 6L8 10L12 6"
        stroke="#94A3B8"
        strokeWidth="1.33333"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </StyledSVG>
  );
}

export default ChevronIcon;

const StyledSVG = styled.svg<{ direction: ChevronIconProps['direction'] }>`
  transform: ${({ direction }) => directionRotationMap[direction]};
  transition: transform 0.2s ease;
`;
