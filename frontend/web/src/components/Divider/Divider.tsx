import styled from '@emotion/styled';

interface DividerProps {
  margin?: number;
  height?: number;
}

const Divider = ({ margin = 8, height = 1 }: DividerProps) => {
  return <StyledDivider margin={margin} height={height} />;
};

export default Divider;

const StyledDivider = styled.div<{ margin: number; height: number }>`
  width: 100%;
  height: ${({ height }) => height}px;
  margin: ${({ margin }) => margin}px 0;

  background-color: ${({ theme }) => theme.colors.dividers};
`;
