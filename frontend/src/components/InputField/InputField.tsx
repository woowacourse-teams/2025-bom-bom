import styled from '@emotion/styled';
import { ChangeEvent, ReactNode } from 'react';

interface InputFiledProps {
  name?: string;
  label?: string | ReactNode;
  inputValue: string;
  onInputChange: (e: ChangeEvent<HTMLInputElement>) => void;
  placeholder?: string;
  errorString?: string;
  InputSuffixComponent?: ReactNode;
}

const InputField = ({
  name,
  label,
  inputValue,
  onInputChange,
  placeholder,
  errorString,
  InputSuffixComponent,
}: InputFiledProps) => {
  return (
    <FieldGroup>
      {typeof label === 'string' ? (
        <Label htmlFor={name}>{label}</Label>
      ) : (
        label
      )}
      <InputRow>
        <Input
          id={name}
          name={name}
          value={inputValue}
          onChange={onInputChange}
          placeholder={placeholder ?? ''}
          aria-invalid={!!errorString}
          aria-describedby={errorString ? `${name}-error` : undefined}
        />
        <InputSuffixWrapper>{InputSuffixComponent}</InputSuffixWrapper>
      </InputRow>
      {errorString && <Error id={`${name}-error`}>{errorString}</Error>}
    </FieldGroup>
  );
};

export default InputField;

const FieldGroup = styled.div`
  display: flex;
  gap: 8px;
  flex-direction: column;
`;

const Label = styled.label`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const Input = styled.input`
  width: 100%;
  height: 48px;
  padding: 12px 14px;
  outline: none;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 12px;

  background: ${({ theme }) => theme.colors.white};
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
`;

const InputRow = styled.div`
  position: relative;

  display: flex;
  align-items: stretch;
`;

const InputSuffixWrapper = styled.div`
  position: absolute;
  top: 6px;
  right: 10px;
  width: fit-content;
  height: calc(100% - 12px);
`;

const Error = styled.div`
  color: #d72d3f;
  font: ${({ theme }) => theme.fonts.caption};
`;
