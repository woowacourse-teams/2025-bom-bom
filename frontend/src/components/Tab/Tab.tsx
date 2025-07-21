import styled from '@emotion/styled';
import { ReactNode } from 'react';

export interface TabProps {
  id: string | number;
  onSelect: (id: string | number) => void;
  selected?: boolean;
  text: string;
  LeadingComponent?: ReactNode;
  TrailingComponent?: ReactNode;
}

function Tab({
  id,
  selected = false,
  onSelect,
  text,
  LeadingComponent,
  TrailingComponent,
}: TabProps) {
  return (
    <Container selected={selected} onClick={() => onSelect(id)}>
      {LeadingComponent}
      {text}
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
