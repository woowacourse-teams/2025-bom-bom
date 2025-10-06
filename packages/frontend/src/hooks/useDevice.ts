import { DEVICE_BREAKPOINTS } from '@/constants/device';
import useMediaQuery from '@/hooks/useMediaQuery';

export type Device = 'mobile' | 'tablet' | 'pc';

export function useDevice() {
  const isMobile = useMediaQuery({
    key: 'max-width',
    value: DEVICE_BREAKPOINTS.mobileMax,
  });
  const isTablet = useMediaQuery({
    key: 'max-width',
    value: DEVICE_BREAKPOINTS.tabletMax,
  });

  const type: Device = isMobile ? 'mobile' : isTablet ? 'tablet' : 'pc';

  return type;
}
