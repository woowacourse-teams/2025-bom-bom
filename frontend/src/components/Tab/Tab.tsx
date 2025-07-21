import styled from '@emotion/styled';
import { ReactNode } from 'react';

export interface TabProps {
  name: string;
  onSelect: (name: string) => void;
  selected?: boolean;
  LeadingComponent?: ReactNode;
  TrailingComponent?: ReactNode;
}

function Tab({
  name,
  selected = false,
  onSelect,
  LeadingComponent,
  TrailingComponent,
}: TabProps) {
  return (
    <Container selected={selected} onClick={() => onSelect(name)}>
      {LeadingComponent}
      {name}
      {TrailingComponent}
    </Container>
  );
}

export default Tab;

const Container = styled.li<{ selected: boolean }>`
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;

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
