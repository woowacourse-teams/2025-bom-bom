import styled from '@emotion/styled';

interface TabProps {
  id: string | number;
  onSelect: (id: string | number) => void;
  selected?: boolean;
  children?: React.ReactNode;
}

function Tab({ id, selected = false, onSelect, children }: TabProps) {
  return (
    <Container selected={selected} onClick={() => onSelect(id)}>
      {children}
    </Container>
  );
}

export default Tab;

const Container = styled.div<{ selected: boolean }>`
  display: flex;
  align-items: center;
  justify-content: center;

  padding: 10px 14px;
  border-radius: 14px;

  background-color: ${({ selected, theme }) =>
    selected ? theme.colors.primary : theme.colors.white};

  color: ${({ selected, theme }) =>
    selected ? theme.colors.white : theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
  font-weight: ${({ selected }) => (selected ? '600' : '400')};

  cursor: pointer;
`;
