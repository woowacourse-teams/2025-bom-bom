import { directionRotationMap } from './Icons.constants';
import { DirectionType } from './Icons.types';

export const calculateDirection = (
  targetDirection: DirectionType,
  currentDirection: DirectionType,
) => {
  const targetDegree = directionRotationMap[targetDirection];
  const currentDegree = directionRotationMap[currentDirection];
  const degree = (targetDegree - currentDegree) % 360;
  return `rotate(${degree}deg)`;
};
