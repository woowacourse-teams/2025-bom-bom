import styled from '@emotion/styled';
import { ComponentProps } from 'react';
import type { Theme } from '@emotion/react';

type VariantType = 'default' | 'outlinePrimary';

interface BadgeProps extends ComponentProps<'div'> {
  text: string;
  variant?: VariantType;
}

function Badge({ text, variant = 'default', className, ...props }: BadgeProps) {
  return (
    <Container variant={variant} className={className} {...props}>
      {text}
    </Container>
  );
}

export default Badge;

const Container = styled.div<{ variant: VariantType }>`
  width: fit-content;
  padding: 4px 8px;
  border-radius: 8px;

  display: flex;
  align-items: center;
  justify-content: center;

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
    border: `2px solid ${theme.colors.white}`,
    backgroundColor: theme.colors.primary,
    color: theme.colors.white,
    fontWeight: 600,
  }),
};
