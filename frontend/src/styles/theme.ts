const fonts = {
  heading1: {
    fontSize: '48px',
    lineHeight: '60px',
    fontWeight: '700',
  },
  heading2: {
    fontSize: '34px',
    lineHeight: '50px',
    fontWeight: '700',
  },
  heading3: {
    fontSize: '28px',
    lineHeight: '38px',
    fontWeight: '600',
  },
  heading4: {
    fontSize: '24px',
    lineHeight: '32px',
    fontWeight: '600',
  },
  heading5: {
    fontSize: '18px',
    lineHeight: '28px',
    fontWeight: '600',
  },
  heading6: {
    fontSize: '16px',
    lineHeight: '24px',
    fontWeight: '600',
  },
  body1: {
    fontSize: '16px',
    lineHeight: '24px',
    fontWeight: '400',
  },
  body2: {
    fontSize: '14px',
    lineHeight: '22px',
    fontWeight: '400',
  },
  caption: {
    fontSize: '12px',
    lineHeight: '18px',
    fontWeight: '400',
  },
};

const colors = {
  primary: '#FF9966',
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
};

export const theme = {
  fonts,
  colors,
};

export type AppTheme = typeof theme;
