import styled from '@emotion/styled';

interface ChipProps {
  text: string;
  selected?: boolean;
  onSelect?: () => void;
}

function Chip({ text, selected = false, onSelect }: ChipProps) {
  return (
    <Container type="button" selected={selected} onClick={onSelect}>
      {text}
    </Container>
  );
}

export default Chip;

const Container = styled.button<{
  selected: boolean;
}>`
  display: flex;
  justify-content: center;
  align-items: center;

  width: fit-content;
  padding: 8px 16px;

  ${({ theme }) => theme.fonts.caption};

  color: ${({ theme, selected }) =>
    selected ? theme.colors.white : theme.colors.textPrimary};
  background-color: ${({ theme, selected }) =>
    selected ? theme.colors.black : theme.colors.dividers};
  border-radius: 16px;
`;
