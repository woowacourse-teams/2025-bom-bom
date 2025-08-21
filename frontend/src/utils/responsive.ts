import { DeviceType } from '@/hooks/useDeviceType';

export const getResponsiveValue = (
  deviceType: DeviceType,
  mobile: number,
  tablet: number,
  pc: number,
) => {
  switch (deviceType) {
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
