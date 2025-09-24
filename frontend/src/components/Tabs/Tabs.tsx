import styled from '@emotion/styled';
import { ComponentProps, ReactElement } from 'react';

type Direction = 'horizontal' | 'vertical';

interface TabsProps extends ComponentProps<'ul'> {
  direction?: Direction;
  children:
    | ReactElement<ComponentProps<'li'>>[]
    | ReactElement<ComponentProps<'li'>>;
}

const Tabs = ({ direction = 'horizontal', children, ...props }: TabsProps) => {
  return (
    <Container role="tablist" direction={direction} {...props}>
      {children}
    </Container>
  );
};

export default Tabs;

const Container = styled.ul<{ direction: Direction }>`
  display: flex;
  gap: ${({ direction }) => (direction === 'horizontal' ? '12px' : '8px')};
  flex-direction: ${({ direction }) =>
    direction === 'horizontal' ? 'row' : 'column'};
`;
