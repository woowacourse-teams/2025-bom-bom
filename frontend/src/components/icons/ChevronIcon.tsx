import styled from '@emotion/styled';
import { SVGProps } from 'react';
import { Direction } from './Icons.types';
import ChevronDownSvg from '#/assets/svg/chevron-down.svg';

interface ChevronIconProps extends SVGProps<SVGSVGElement> {
  direction?: Direction;
}

const rotationMap: Record<Direction, number> = {
  up: 180,
  upRight: -45,
  right: -90,
  downRight: -135,
  down: 0,
  downLeft: 135,
  left: 90,
  upLeft: 45,
};

const ChevronIcon = ({
  direction = 'down',
  className,
  ...props
}: ChevronIconProps) => {
  return (
    <Wrapper className={className} rotation={rotationMap[direction]}>
      <ChevronDownSvg {...props} />
    </Wrapper>
  );
};

export default ChevronIcon;

const Wrapper = styled.span<{
  rotation: number;
}>`
  width: fit-content;
  height: fit-content;

  display: inline-flex;

  transform: rotate(${({ rotation }) => rotation}deg);
  transition: transform 0.2s ease-in-out;
`;
