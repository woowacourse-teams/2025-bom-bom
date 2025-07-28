import styled from '@emotion/styled';
import { SVGProps } from 'react';
import { DirectionType } from './Icons.types';
import { calculateDirection } from './Icons.utils';

interface ArrowIconProps extends SVGProps<SVGSVGElement> {
  targetDirection: DirectionType;
  currentDirection: DirectionType;
}

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

const StyledSVG = styled.svg<{
  targetDirection: DirectionType;
  currentDirection: DirectionType;
}>`
  transform: ${({ targetDirection, currentDirection }) =>
    calculateDirection(targetDirection, currentDirection)};
  transform-origin: center center;
`;
