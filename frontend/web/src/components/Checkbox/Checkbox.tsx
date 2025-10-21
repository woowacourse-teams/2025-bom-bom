import styled from '@emotion/styled';
import type { ChangeEvent, PropsWithChildren } from 'react';

interface CheckboxProps {
  id: string;
  checked: boolean;
  onChange: (e: ChangeEvent<HTMLInputElement>) => void;
}

const Checkbox = ({
  id,
  checked,
  onChange,
  children,
}: PropsWithChildren<CheckboxProps>) => {
  return (
    <CheckboxWrapper>
      <HiddenCheckbox
        id={String(id)}
        type="checkbox"
        checked={checked}
        onChange={onChange}
      />
      <CheckboxLabel htmlFor={String(id)} checked={checked}>
        <CheckboxSquare checked={checked}>
          {checked && <CheckMark>✓</CheckMark>}
        </CheckboxSquare>
        {children}
      </CheckboxLabel>
    </CheckboxWrapper>
  );
};

export default Checkbox;

const CheckboxWrapper = styled.div`
  display: flex;
  align-items: center;
`;

const HiddenCheckbox = styled.input`
  position: absolute;
  width: 1px;
  height: 1px;
  margin: 0;
  padding: 0;
  border: 0;

  appearance: none;
  opacity: 0;
  pointer-events: none;

  &:focus-visible + label {
    outline: 2px solid ${({ theme }) => theme.colors.primary};
    outline-offset: 2px;
  }
`;

const CheckboxLabel = styled.label<{ checked: boolean }>`
  display: flex;
  gap: 8px;
  align-items: center;

  cursor: pointer;
  user-select: none;
`;

const CheckboxSquare = styled.div<{ checked: boolean }>`
  width: 20px;
  height: 20px;
  border: 2px solid
    ${({ theme, checked }) =>
      checked ? theme.colors.primary : theme.colors.stroke};
  border-radius: 4px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme, checked }) =>
    checked ? theme.colors.primary : theme.colors.white};

  transition: all 0.2s ease;
`;

const CheckMark = styled.span`
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.body2};
`;
