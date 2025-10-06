import type { Device } from '@/hooks/useDevice';

export const getResponsiveValue = (
  device: Device,
  mobile: number,
  tablet: number,
  pc: number,
) => {
  switch (device) {
    case 'mobile':
      return `${mobile}px`;
    case 'tablet':
      return `${tablet}px`;
    case 'pc':
      return `${pc}px`;
    default:
      return `${mobile}px`;
  }
};
