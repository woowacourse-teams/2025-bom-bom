import BookmarkIcon from '#/assets/svg/bookmark-inactive.svg';
import HelpIcon from '#/assets/svg/help.svg';
import MemoIcon from '#/assets/svg/memo.svg';

export const MENU_ITEMS = [
  {
    key: 'bookmark',
    label: '북마크',
    path: '/bookmark',
    Icon: BookmarkIcon,
  },
  {
    key: 'memo',
    label: '메모',
    path: '/memo',
    Icon: MemoIcon,
  },
  {
    key: 'guide',
    label: '가이드',
    path: '/guide',
    Icon: HelpIcon,
  },
] as const;
