import { keyframes } from '@emotion/react';

const shimmer = keyframes`
  0% { background-position: -400px 0; }
  100% { background-position: 400px 0; }
`;

export const skeletonStyle = {
  background: 'linear-gradient(90deg, #e0e0e0 25%, #f0f0f0 37%, #e0e0e0 63%)',
  backgroundSize: '400px 100%',
  animation: `${shimmer} 1.4s ease infinite`,
};
