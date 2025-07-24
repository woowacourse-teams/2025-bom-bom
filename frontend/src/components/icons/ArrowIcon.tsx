import styled from '@emotion/styled';
import { SVGProps } from 'react';

interface ArrowIconProps extends SVGProps<SVGSVGElement> {
  direction:
    | 'topRight'
    | 'right'
    | 'downRight'
    | 'down'
    | 'downLeft'
    | 'left'
    | 'upLeft'
    | 'up';
}

const directionRotationMap = {
  topRight: 'rotate(0deg)',
  right: 'rotate(45deg)',
  downRight: 'rotate(90deg)',
  down: 'rotate(135deg)',
  downLeft: 'rotate(180deg)',
  left: 'rotate(225deg)',
  upLeft: 'rotate(270deg)',
  up: 'rotate(315deg)',
};

export default function ArrowIcon({ direction, ...props }: ArrowIconProps) {
  return (
    <StyledSVG
      {...props}
      direction={direction}
      xmlns="http://www.w3.org/2000/svg"
      height="24px"
      viewBox="0 -960 960 960"
      width="24px"
      fill="#ffffff"
    >
      <path d="m216-160-56-56 464-464H360v-80h400v400h-80v-264L216-160Z" />
    </StyledSVG>
  );
}

const StyledSVG = styled.svg<{ direction: ArrowIconProps['direction'] }>`
  transform: ${({ direction }) => directionRotationMap[direction]};
  transform-origin: center center;
`;
