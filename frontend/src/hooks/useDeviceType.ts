import useMediaQuery from '@/hooks/useMediaQuery';

const BREAKPOINTS = {
  mobileMax: 768,
  tabletMax: 1024,
} as const;

export type DeviceType = 'mobile' | 'tablet' | 'pc';

export function useDeviceType() {
  const isMobile = useMediaQuery({
    key: 'max-width',
    value: BREAKPOINTS.mobileMax,
  });
  const isTablet = useMediaQuery({
    key: 'max-width',
    value: BREAKPOINTS.tabletMax,
  });

  const type: DeviceType = isMobile ? 'mobile' : isTablet ? 'tablet' : 'pc';

  return type;
}
