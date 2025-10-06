const fontFamily =
  '"Pretendard Variable", Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol"';

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
  red: '#f00000',
  error: '#FF4D4F',
  info: '#0D6EFD',
  success: '#198754',
};

const heights = {
  headerPC: '72px',
  headerMobile: '56px',
  bottomNav: '64px',
};

const zIndex = {
  behind: -1,
  base: 0,
  content: 1,
  elevated: 10,
  panel: 50,
  header: 100,
  floating: 800,
  overlay: 1000,
  toast: 9000,
};

export const theme = {
  fonts,
  colors,
  heights,
  zIndex,
};

export type AppTheme = typeof theme;
