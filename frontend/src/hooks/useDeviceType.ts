import { DEVICE_BREAKPOINTS } from '@/constants/device';
import useMediaQuery from '@/hooks/useMediaQuery';

export type DeviceType = 'mobile' | 'tablet' | 'pc';

export function useDeviceType() {
  const isMobile = useMediaQuery({
    key: 'max-width',
    value: DEVICE_BREAKPOINTS.mobileMax,
  });
  const isTablet = useMediaQuery({
    key: 'max-width',
    value: DEVICE_BREAKPOINTS.tabletMax,
  });

  const type: DeviceType = isMobile ? 'mobile' : isTablet ? 'tablet' : 'pc';

  return type;
}
