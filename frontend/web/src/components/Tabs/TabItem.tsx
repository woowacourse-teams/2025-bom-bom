import styled from '@emotion/styled';
import type { Device } from '@/hooks/useDevice';
import type { ComponentProps } from 'react';

interface TabItemProps extends ComponentProps<'li'> {
  isActive: boolean;
  device: Device;
  children: React.ReactNode;
}

const TabItem = ({ isActive, device, children, ...props }: TabItemProps) => {
  return (
    <StyledTabItem role="tab" aria-selected={isActive} {...props}>
      <TabButton
        type="button"
        isActive={isActive}
        device={device}
        tabIndex={isActive ? 0 : -1}
      >
        {children}
      </TabButton>
    </StyledTabItem>
  );
};

export default TabItem;

const StyledTabItem = styled.li`
  list-style: none;
`;

const TabButton = styled.button<{ isActive: boolean; device: Device }>`
  padding: ${({ device }) => (device === 'mobile' ? '12px 16px' : '16px 24px')};

  color: ${({ theme, isActive }) =>
    isActive ? theme.colors.textPrimary : theme.colors.textSecondary};
  font: ${({ theme, device }) =>
    device === 'mobile' ? theme.fonts.body2 : theme.fonts.body1};
  font-weight: ${({ isActive }) => (isActive ? 600 : 400)};

  border: none;
  border-bottom: 2px solid
    ${({ theme, isActive }) =>
      isActive ? theme.colors.primary : 'transparent'};

  background: transparent;
  cursor: pointer;

  transition: all 0.2s ease-in-out;

  &:hover {
    color: ${({ theme }) => theme.colors.textPrimary};
  }

  &:focus-visible {
    outline: 2px solid ${({ theme }) => theme.colors.primary};
    outline-offset: 2px;
  }
`;
