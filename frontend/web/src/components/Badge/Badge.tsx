import styled from '@emotion/styled';
import type { Theme } from '@emotion/react';
import type { ComponentProps } from 'react';

type Variant = 'default' | 'outlinePrimary';

interface BadgeProps extends ComponentProps<'div'> {
  text: string;
  variant?: Variant;
  icon?: React.ReactNode;
}

function Badge({ text, variant = 'default', icon, ...props }: BadgeProps) {
  return (
    <Container variant={variant} {...props}>
      {icon && <IconWrapper>{icon}</IconWrapper>}

      {text}
    </Container>
  );
}

export default Badge;

const IconWrapper = styled.span`
  display: inline-flex;
  margin-right: 4px;
  line-height: 0;
`;

const Container = styled.div<{ variant: Variant }>`
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
