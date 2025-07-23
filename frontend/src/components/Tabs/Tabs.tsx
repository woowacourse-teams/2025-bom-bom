import styled from '@emotion/styled';
import { ComponentProps, ReactElement } from 'react';
import { TabProps } from '../Tab/Tab';

type DirectionType = 'horizontal' | 'vertical';

interface TabsProps<T extends string> extends ComponentProps<'ul'> {
  direction?: DirectionType;
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

const Container = styled.ul<{ direction: DirectionType }>`
  display: flex;
  gap: 8px;
  flex-direction: ${({ direction }) =>
    direction === 'horizontal' ? 'row' : 'column'};
`;
