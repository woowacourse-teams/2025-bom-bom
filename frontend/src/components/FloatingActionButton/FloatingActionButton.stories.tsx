import styled from '@emotion/styled';
import FloatingActionButton from './FloatingActionButton';
import { theme } from '@/styles/theme';
import type { Meta, StoryObj } from '@storybook/react-webpack5';
import BookmarkIcon from '#/assets/bookmark-inactive.svg';
import LinkIcon from '#/assets/link.svg';
import MemoIcon from '#/assets/memo.svg';

const meta: Meta<typeof FloatingActionButton> = {
  title: 'Components/Common/FloatingActionButton',
  component: FloatingActionButton,
  parameters: {
    layout: 'padded',
    docs: {
      story: {
        inline: false,
        height: '200px',
      },
      canvas: {
        height: '200px',
      },
    },
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

const StyledBookmarkIcon = styled(BookmarkIcon)`
  width: 20px;
  height: 20px;

  color: ${({ theme }) => theme.colors.primary};
`;

const LinkText = styled.span`
  font: ${({ theme }) => theme.fonts.body1};
`;

const StyledLink = styled.button`
  padding: 8px;
  border-radius: 8px;

  display: flex;
  gap: 4px;
  align-items: center;
  justify-content: center;
`;

export const Default: Story = {
  render: () => (
    <FloatingActionButton
      icon={<LinkIcon width={24} height={24} fill="white" />}
    >
      <StyledLink>
        <StyledBookmarkIcon />
        <LinkText>북마크</LinkText>
      </StyledLink>
      <StyledLink>
        <MemoIcon width={20} height={20} fill={theme.colors.primary} />
        <LinkText>메모</LinkText>
      </StyledLink>
    </FloatingActionButton>
  ),
};
