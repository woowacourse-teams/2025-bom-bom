import styled from '@emotion/styled';
import { SVGProps } from 'react';
import { DirectionType } from './Icons.types';
import ArrowRightSvg from '#/assets/arrow-right.svg';

interface ArrowIconProps extends SVGProps<SVGSVGElement> {
  direction?: DirectionType;
}

export const rotationMap = {
  up: -90,
  upRight: -45,
  right: 0,
  downRight: 45,
  down: 90,
  downLeft: 135,
  left: 180,
  upLeft: 225,
};

export default function ArrowIcon({
  direction = 'upRight',
  className,
  ...props
}: ArrowIconProps) {
  return (
    <Wrapper className={className} rotation={rotationMap[direction]}>
      <ArrowRightSvg {...props} />
    </Wrapper>
  );
}

const Wrapper = styled.span<{
  rotation: number;
}>`
  width: fit-content;
  height: fit-content;

  display: inline-flex;

  transform: rotate(${({ rotation }) => rotation}deg);
  transition: transform 0.2s ease-in-out;
`;
