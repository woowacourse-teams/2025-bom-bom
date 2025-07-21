import styled from '@emotion/styled';
import { ComponentProps } from 'react';

type DirectionType = 'horizontal' | 'vertical';

interface TabsProps extends ComponentProps<'ul'> {
  direction?: DirectionType;
  children: React.ReactNode;
}

const Tabs = ({ direction = 'horizontal', children, ...props }: TabsProps) => {
  return (
    <Container role="tablist" direction={direction} {...props}>
      {children}
    </Container>
  );
};

export default Tabs;

const Container = styled.ul<{ direction: DirectionType }>`
  display: flex;
  flex-direction: ${({ direction }) =>
    direction === 'horizontal' ? 'row' : 'column'};
  gap: 8px;
`;
