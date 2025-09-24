import styled from '@emotion/styled';
import EditableMemoCard from '../MemoCard/EditableMemoCard';
import ChevronIcon from '@/components/icons/ChevronIcon';
import { theme } from '@/styles/theme';
import type { Highlight } from '../../types/highlight';
import CloseIcon from '#/assets/svg/close.svg';
import MemoIcon from '#/assets/svg/memo.svg';

interface MemoPanelProps {
  opened: boolean;
  memos: Highlight[];
  removeHighlight: ({ id }: { id: number }) => void;
  updateMemo: (id: number, memo: string) => void;
  onCloseButtonClick: () => void;
  onToggleButtonClick: () => void;
}

const MemoPanel = ({
  opened,
  memos,
  removeHighlight,
  updateMemo,
  onCloseButtonClick,
  onToggleButtonClick,
}: MemoPanelProps) => {
  return (
    <Container opened={opened}>
      <ToggleButton opened={opened} onClick={onToggleButtonClick}>
        <ChevronIcon
          direction={opened ? 'right' : 'left'}
          width={24}
          height={24}
          color={theme.colors.primary}
        />
      </ToggleButton>

      <Header>
        <HeaderLeft>
          <IconWrapper>
            <MemoIcon fill={theme.colors.primary} width={24} height={24} />
          </IconWrapper>
          <HeaderTitleBox>
            <HeaderTitleText>읽기 노트</HeaderTitleText>
            <HeaderTitleCaption>{memos.length}개의 메모</HeaderTitleCaption>
          </HeaderTitleBox>
        </HeaderLeft>

        <CloseButton onClick={onCloseButtonClick}>
          <CloseIcon fill={theme.colors.black} />
        </CloseButton>
      </Header>

      <NotesList>
        {memos.length === 0 ? (
          <EmptyWrapper>
            <EmptyIconWrapper>
              <MemoIcon width={36} height={36} fill={theme.colors.primary} />
            </EmptyIconWrapper>
            <HeaderTitleText>아직 메모가 없어요</HeaderTitleText>
            <HeaderTitleCaption>
              중요한 내용을 메모로 기록해보세요
            </HeaderTitleCaption>
          </EmptyWrapper>
        ) : (
          memos?.map((note) => (
            <EditableMemoCard
              key={note.id}
              id={note.id}
              content={note.text}
              memo={note.memo}
              onRemoveButtonClick={removeHighlight}
              onMemoChange={updateMemo}
            />
          ))
        )}
      </NotesList>
    </Container>
  );
};

export default MemoPanel;

const Container = styled.aside<{ opened: boolean }>`
  position: fixed;
  top: 0;
  right: 0;
  z-index: ${({ theme }) => theme.zIndex.panel};
  width: 342px;
  height: 100%;
  padding-top: 72px;
  border-left: 1px solid ${({ theme }) => theme.colors.stroke};

  display: flex;
  flex-direction: column;

  background-color: ${({ theme }) => theme.colors.white};

  transform: ${({ opened }) => (opened ? 'translateX(0)' : 'translateX(100%)')};

  transition: transform 0.3s;
`;

const ToggleButton = styled.button<{ opened: boolean }>`
  position: absolute;
  top: 80vh;
  left: -40px;
  width: 40px;
  height: 80px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 8px 0 0 8px;

  background-color: ${({ theme }) => theme.colors.white};

  transform: translateY(-50%);
`;

const Header = styled.div`
  padding: 24px;
  border-bottom: 1px solid ${({ theme }) => theme.colors.dividers};

  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const HeaderLeft = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
`;
const HeaderTitleBox = styled.div``;

const HeaderTitleText = styled.h5`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const HeaderTitleCaption = styled.h5`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const IconWrapper = styled.div`
  padding: 6px;
  border-radius: 50%;
  box-shadow: 0 1px 2px rgb(0 0 0 / 5%);

  background-color: ${({ theme }) => `${theme.colors.primary}10`};
`;

const CloseButton = styled.button`
  padding: 6px;
`;

const NotesList = styled.div`
  padding: 24px;

  display: flex;
  gap: 16px;
  flex: 1;
  flex-direction: column;

  overflow-y: auto;
`;

const EmptyWrapper = styled.div`
  padding-top: 80px;
  padding-bottom: 80px;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const EmptyIconWrapper = styled.div`
  margin-bottom: 16px;
  padding: 16px;
  border-radius: 50%;

  background-color: ${({ theme }) => `${theme.colors.primary}10`};
`;
