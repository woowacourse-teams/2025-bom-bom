import styled from '@emotion/styled';

interface DividerProps {
  margin?: number;
}

const Divider = ({ margin = 8 }: DividerProps) => {
  return <StyledDivider margin={margin} />;
};

export default Divider;

const StyledDivider = styled.div<{ margin: number }>`
  width: 100%;
  height: 1px;
  margin: ${({ margin }) => margin}px 0;

  background-color: ${({ theme }) => theme.colors.dividers};
`;
