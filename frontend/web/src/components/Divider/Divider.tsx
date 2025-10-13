import styled from '@emotion/styled';

interface DividerProps {
  margin?: string;
}

const Divider = ({ margin = '8px 0' }: DividerProps) => {
  return <StyledDivider margin={margin} />;
};

export default Divider;

const StyledDivider = styled.div<{ margin: string }>`
  width: 100%;
  height: 1px;
  margin: ${({ margin }) => margin};

  background-color: ${({ theme }) => theme.colors.dividers};
`;
