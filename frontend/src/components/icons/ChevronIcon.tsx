import styled from '@emotion/styled';
import { SVGProps } from 'react';
import { DirectionType } from './Icons.types';
import { calculateDirection } from './Icons.utils';

interface ChevronIconProps extends SVGProps<SVGSVGElement> {
  targetDirection: DirectionType;
  currentDirection: DirectionType;
}

function ChevronIcon({
  targetDirection,
  currentDirection,
  ...props
}: ChevronIconProps) {
  return (
    <StyledSVG
      targetDirection={targetDirection}
      currentDirection={currentDirection}
      width="16"
      height="16"
      viewBox="0 0 16 16"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
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

const StyledSVG = styled.svg<{
  targetDirection: DirectionType;
  currentDirection: DirectionType;
}>`
  transform: ${({ targetDirection, currentDirection }) =>
    calculateDirection(targetDirection, currentDirection)};
  transition: transform 0.2s ease;
`;
