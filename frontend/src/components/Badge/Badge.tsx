import styled from '@emotion/styled';
import type { Theme } from '@emotion/react';

type VariantType = 'default' | 'outlinePrimary';

interface BadgeProps {
  text: string;
  variant?: VariantType;
}

function Badge({ text, variant = 'default' }: BadgeProps) {
  return <Container variant={variant}>{text}</Container>;
}

export default Badge;

const Container = styled.div<{ variant: VariantType }>`
  display: flex;
  align-items: center;
  justify-content: center;

  width: fit-content;
  padding: 4px 8px;
  border-radius: 8px;

  font: ${({ theme }) => theme.fonts.caption};

  ${({ variant, theme }) => variantStyles[variant](theme)}
`;

const variantStyles = {
  default: (theme: Theme) => ({
    border: 'none',
    backgroundColor: theme.colors.dividers,
    color: theme.colors.textPrimary,
  }),
  outlinePrimary: (theme: Theme) => ({
    border: `1px solid ${theme.colors.white}`,
    backgroundColor: theme.colors.primary,
    color: theme.colors.white,
  }),
};
