import styled from '@emotion/styled';
import type { TabProps } from '../Tab/Tab';
import type { ComponentProps, ReactElement } from 'react';

type Direction = 'horizontal' | 'vertical';

interface TabsProps<T extends string> extends ComponentProps<'ul'> {
  direction?: Direction;
  children: ReactElement<TabProps<T>>[];
}

const Tabs = <T extends string>({
  direction = 'horizontal',
  children,
  ...props
}: TabsProps<T>) => {
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
