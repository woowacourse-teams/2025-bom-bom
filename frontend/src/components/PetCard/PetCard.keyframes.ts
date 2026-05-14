import { keyframes } from '@emotion/react';

export const jumpAnimation = keyframes`
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
`;

export const heartAnimation = keyframes`
  0% { 
    opacity: 0; 
    transform: translateY(0) scale(0.5); 
  }
  50% { 
    opacity: 1; 
    transform: translateY(-20px) scale(1.2); 
  }
  100% { 
    opacity: 0; 
    transform: translateY(-40px) scale(0.8); 
  }
`;
