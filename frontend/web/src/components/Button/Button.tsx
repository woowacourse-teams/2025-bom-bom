import { css, type Theme } from '@emotion/react';
import styled from '@emotion/styled';
import type { ComponentProps } from 'react';

type Variant = 'default' | 'outlined';

interface ButtonProps extends ComponentProps<'button'> {
  text: string;
  onClick: () => void;
  variant?: Variant;
}

const Button = ({
  text,
  onClick,
  variant = 'default',
  ...props
}: ButtonProps) => {
  return (
    <Container type="button" onClick={onClick} variant={variant} {...props}>
      {text}
    </Container>
  );
};

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
  default: (theme: Theme) => css`
    border: none;

    background-color: ${theme.colors.primary};
    color: ${theme.colors.white};

    &:hover {
      background-color: ${theme.colors.primaryDark};
    }
  `,
  outlined: (theme: Theme) => css`
    border: 1px solid ${theme.colors.stroke};

    background-color: ${theme.colors.white};
    color: ${theme.colors.textPrimary};

    &:hover {
      background-color: ${theme.colors.disabledBackground};
    }
  `,
};
