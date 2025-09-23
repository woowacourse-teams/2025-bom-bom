import styled from '@emotion/styled';
import { ComponentProps } from 'react';
import type { Theme } from '@emotion/react';

type Variant = 'default';

interface ButtonProps extends ComponentProps<'button'> {
  text: string;
  onClick: () => void;
  variant?: Variant;
}

function Button({ text, onClick, variant = 'default', ...props }: ButtonProps) {
  return (
    <Container type="button" onClick={onClick} variant={variant} {...props}>
      {text}
    </Container>
  );
}

export default Button;

const Container = styled.button<{
  variant: Variant;
}>`
  width: fit-content;
  padding: 8px 16px;
  border-radius: 16px;

  display: flex;
  align-items: center;
  justify-content: center;

  font: ${({ theme }) => theme.fonts.caption};

  ${({ variant, theme }) => variantStyles[variant](theme)}

  &:disabled {
    background-color: ${({ theme }) => theme.colors.disabledBackground};
    color: ${({ theme }) => theme.colors.disabledText};

    cursor: not-allowed;
    opacity: 0.5;
  }
`;

const variantStyles = {
  default: (theme: Theme) => ({
    border: 'none',
    backgroundColor: theme.colors.primary,
    color: theme.colors.white,
  }),
};
