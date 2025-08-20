import styled from '@emotion/styled';
import { ReactNode, LiHTMLAttributes } from 'react';

export interface TabProps<T>
  extends Omit<LiHTMLAttributes<HTMLLIElement>, 'value'> {
  value: T;
  label: string;
  onTabSelect: (value: T) => void;
  selected?: boolean;
  StartComponent?: ReactNode;
  EndComponent?: ReactNode;
  textAlign?: 'start' | 'center' | 'end';
}

function Tab<T>({
  value,
  label,
  onTabSelect,
  selected = false,
  StartComponent,
  EndComponent,
  textAlign = 'center',
  ...props
}: TabProps<T>) {
  return (
    <Container
      selected={selected}
      onClick={() => onTabSelect(value)}
      {...props}
    >
      {StartComponent}
      <Label textAlign={textAlign}>{label}</Label>
      {EndComponent}
    </Container>
  );
}

export default Tab;

const Container = styled.li<{ selected: boolean }>`
  min-width: fit-content;
  padding: 10px 12px;
  border: 2px solid
    ${({ selected, theme }) =>
      selected ? theme.colors.primary : 'transparent'};
  border-radius: 12px;

  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;

  background-color: ${({ selected, theme }) =>
    selected ? theme.colors.primary : theme.colors.white};
  color: ${({ selected, theme }) =>
    selected ? theme.colors.white : theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
  font-weight: ${({ selected }) => (selected ? '600' : '400')};

  cursor: pointer;
  transition: all 0.2s ease-in-out;

  &:hover {
    background-color: ${({ selected, theme }) =>
      selected ? theme.colors.primary : theme.colors.disabledBackground};
  }
`;

const Label = styled.span<{ textAlign: 'start' | 'center' | 'end' }>`
  width: 100%;
  text-align: ${({ textAlign }) => textAlign};
`;
