const fontFamily =
  'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol"';

const fonts = {
  heading1: `700 48px/60px ${fontFamily}`,
  heading2: `700 34px/50px ${fontFamily}`,
  heading3: `700 28px/38px ${fontFamily}`,
  heading4: `600 24px/32px ${fontFamily}`,
  heading5: `600 18px/28px ${fontFamily}`,
  heading6: `600 16px/24px ${fontFamily}`,
  body1: `400 16px/24px ${fontFamily}`,
  body2: `400 14px/22px ${fontFamily}`,
  body3: `400 12px/20px ${fontFamily}`,
  caption: `400 12px/18px ${fontFamily}`,
};

const colors = {
  primary: '#FE5E04',
  primaryLight: '#FFD6C2',
  textPrimary: '#181818',
  textSecondary: '#5C5C5C',
  textTertiary: '#747474',
  icons: '#7C7B7B',
  stroke: '#D7D7D7',
  dividers: '#EDEDED',
  disabledText: '#8A8A8A',
  disabledBackground: '#EFEFEF',
  white: '#FFFFFF',
  black: '#000000',
  error: '#FF4D4F',
};

const heights = {
  headerPC: '72px',
  headerMobile: '56px',
  bottomNav: '64px',
};

const zIndex = {
  behind: -1, // 뒤쪽 배경 요소
  base: 0, // 기본 레벨
  content: 1, // 일반 콘텐츠 요소
  elevated: 10, // 드롭다운, 툴팁, 폼 요소
  panel: 50, // 패널, 사이드바
  header: 100, // 헤더, 네비게이션
  floating: 800, // 플로팅 요소
  overlay: 1000, // 모달, 오버레이, 플로팅 버튼
  toast: 9000, // 토스트, 알림 (최상위)
};

export const theme = {
  fonts,
  colors,
  heights,
  zIndex,
};

export type AppTheme = typeof theme;
