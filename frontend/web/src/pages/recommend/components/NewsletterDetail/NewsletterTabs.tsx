import styled from '@emotion/styled';
import type { NewsletterTab } from './NewsletterDetail.types';

interface NewsletterTabsProps {
  activeTab: NewsletterTab;
  onTabChange: (tab: NewsletterTab) => void;
}

const NewsletterTabs = ({ activeTab, onTabChange }: NewsletterTabsProps) => {
  return (
    <TabHeader>
      <TabButton
        isActive={activeTab === 'detail'}
        onClick={() => onTabChange('detail')}
      >
        뉴스레터 소개
      </TabButton>
      <TabButton
        isActive={activeTab === 'previous'}
        onClick={() => onTabChange('previous')}
      >
        지난 뉴스레터
      </TabButton>
    </TabHeader>
  );
};

export default NewsletterTabs;

const TabHeader = styled.div`
  border-bottom: 1px solid ${({ theme }) => theme.colors.dividers};
  display: flex;
`;

const TabButton = styled.button<{ isActive: boolean }>`
  padding: 12px 0;
  border-bottom: 2px solid
    ${({ theme, isActive }) =>
      isActive ? theme.colors.primary : 'transparent'};

  flex: 1;

  background: none;
  color: ${({ theme, isActive }) =>
    isActive ? theme.colors.primary : theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};

  transition: color 0.2s;

  &:hover {
    color: ${({ theme }) => theme.colors.primary};
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.4;
  }
`;
