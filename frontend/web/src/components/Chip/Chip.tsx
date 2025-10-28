import styled from '@emotion/styled';

interface ChipProps {
  text: string;
  selected?: boolean;
  onSelect?: () => void;
}

function Chip({ text, selected = false, onSelect, ...props }: ChipProps) {
  return (
    <Container type="button" selected={selected} onClick={onSelect} {...props}>
      {text}
    </Container>
  );
}

export default Chip;

const Container = styled.button<{
  selected: boolean;
}>`
  width: fit-content;
  padding: 8px 16px;
  border-radius: 16px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme, selected }) =>
    selected ? theme.colors.black : theme.colors.dividers};
  color: ${({ theme, selected }) =>
    selected ? theme.colors.white : theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.caption};
`;
