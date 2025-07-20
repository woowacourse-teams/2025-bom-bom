import styled from '@emotion/styled';
import { ChangeEvent, InputHTMLAttributes } from 'react';

interface SearchInputProps
  extends Omit<InputHTMLAttributes<HTMLInputElement>, 'onChange'> {
  value?: string;
  onChange?: (value: string) => void;
}

export default function SearchInput({
  value,
  onChange,
  ...props
}: SearchInputProps) {
  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    onChange?.(e.target.value);
  };

  return (
    <Container>
      <StyledInput
        type="text"
        value={value}
        onChange={handleChange}
        aria-label={props['aria-label'] || '검색'}
        {...props}
      />
    </Container>
  );
}

const Container = styled.div`
  position: relative;

  width: 100%;
  height: 42px;
`;

const StyledInput = styled.input`
  width: 100%;
  height: 100%;
  padding: 12px 42px;
  outline: none;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 14px;
  box-shadow:
    0 1px 3px 0 rgb(0 0 0 / 10%),
    0 1px 2px -1px rgb(0 0 0 / 10%);

  background-color: ${({ theme }) => theme.colors.white};

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body1};

  transition: all 0.2s ease-in-out;

  &::placeholder {
    color: ${({ theme }) => theme.colors.textTertiary};
  }

  &:focus {
    box-shadow:
      0 1px 3px 0 rgb(0 0 0 / 10%),
      0 1px 2px -1px rgb(0 0 0 / 10%),
      0 0 0 2px ${({ theme }) => theme.colors.primary}20;
    border-color: ${({ theme }) => theme.colors.primary};
  }

  &:disabled {
    background-color: ${({ theme }) => theme.colors.disabledBackground};

    color: ${({ theme }) => theme.colors.textTertiary};

    cursor: not-allowed;
  }

  &:hover:not(:disabled) {
    border-color: ${({ theme }) => theme.colors.textSecondary};
  }
`;
