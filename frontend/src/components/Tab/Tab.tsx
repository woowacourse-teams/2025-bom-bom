import styled from '@emotion/styled';
import { ComponentProps, ReactNode } from 'react';

export interface TabProps<T extends string> extends ComponentProps<'li'> {
  value: T;
  label: string;
  onTabSelect: (value: T) => void;
  selected?: boolean;
  StartComponent?: ReactNode;
  EndComponent?: ReactNode;
}

function Tab<T extends string>({
  value,
  label,
  onTabSelect,
  selected = false,
  StartComponent,
  EndComponent,
  ...props
}: TabProps<T>) {
  return (
    <Container
      selected={selected}
      onClick={() => onTabSelect(value)}
      {...props}
    >
      {StartComponent}
      {label}
      {EndComponent}
    </Container>
  );
}

export default Tab;

const Container = styled.li<{ selected: boolean }>`
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;

  width: 100%;
  min-width: fit-content;
  padding: 10px 12px;
  border-radius: 12px;

  background-color: ${({ selected, theme }) =>
    selected ? theme.colors.primary : theme.colors.white};

  color: ${({ selected, theme }) =>
    selected ? theme.colors.white : theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
  font-weight: ${({ selected }) => (selected ? '600' : '400')};

  cursor: pointer;
`;
