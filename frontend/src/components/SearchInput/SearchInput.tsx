import styled from '@emotion/styled';
import { ChangeEvent, forwardRef, InputHTMLAttributes } from 'react';

interface SearchInputProps
  extends Omit<InputHTMLAttributes<HTMLInputElement>, 'onChange'> {
  value?: string;
  onChange?: (value: string) => void;
  placeholder?: string;
  disabled?: boolean;
}

const SearchInput = forwardRef<HTMLInputElement, SearchInputProps>(
  (
    {
      value,
      onChange,
      placeholder = '뉴스레터 제목이나 발행처로 검색하세요...',
      disabled,
      ...props
    },
    ref,
  ) => {
    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
      onChange?.(e.target.value);
    };

    return (
      <Container>
        <StyledInput
          ref={ref}
          type="text"
          value={value}
          onChange={handleChange}
          placeholder={placeholder}
          disabled={disabled}
          aria-label="뉴스레터 검색"
          {...props}
        />
      </Container>
    );
  },
);

SearchInput.displayName = 'SearchInput';

export default SearchInput;

const Container = styled.div`
  position: relative;

  width: 100%;
  height: 42px;
`;

const StyledInput = styled.input`
  width: 100%;
  height: 100%;
  padding: 15px 43px;
  outline: none;
  border: 1px solid #d7d7d7;
  border-radius: 14px;
  box-shadow:
    0 1px 3px 0 rgb(0 0 0 / 10%),
    0 1px 2px -1px rgb(0 0 0 / 10%);

  background-color: rgb(255 255 255 / 94%);

  color: ${({ theme }) => theme.colors.textPrimary || '#000'};
  font-family: Inter, 'Noto Sans KR', sans-serif;
  font-weight: 400;
  font-size: 16px;

  transition: all 0.2s ease-in-out;

  &::placeholder {
    color: #717182;
    font-weight: 400;
  }

  &:focus {
    box-shadow:
      0 1px 3px 0 rgb(0 0 0 / 10%),
      0 1px 2px -1px rgb(0 0 0 / 10%),
      0 0 0 2px ${({ theme }) => theme.colors.primary || '#007bff'}20;
    border-color: ${({ theme }) => theme.colors.primary || '#007bff'};
  }

  &:disabled {
    background-color: rgb(255 255 255 / 60%);

    color: #999;

    cursor: not-allowed;
  }

  &:hover:not(:disabled) {
    border-color: ${({ theme }) => theme.colors.textSecondary || '#999'};
  }
`;
