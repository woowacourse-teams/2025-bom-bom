import styled from '@emotion/styled';
import { ChangeEvent } from 'react';
import MemoCard from '../MemoCard/MemoCard';
import { theme } from '@/styles/theme';
import CloseIcon from '#/assets/close.svg';
import MemoIcon from '#/assets/memo.svg';

interface MemoPanelProps {
  open: boolean;
  notes: {
    id: string;
    content: string;
    memo: string;
  }[];
  handleDeleteMemo: (id: string) => void;
  handleUpdateMemo: (id: string, e: ChangeEvent<HTMLTextAreaElement>) => void;
  handleClose: () => void;
}

const MemoPanel = ({
  open,
  notes,
  handleDeleteMemo,
  handleUpdateMemo,
  handleClose,
}: MemoPanelProps) => {
  return (
    <Container isOpen={open}>
      <Header>
        <HeaderLeft>
          <IconWrapper>
            <MemoIcon fill={theme.colors.primary} />
          </IconWrapper>
          <HeaderTitleBox>
            <HeaderTitleText>읽기 노트</HeaderTitleText>
            <HeaderTitleCaption>{notes.length}개의 메모</HeaderTitleCaption>
          </HeaderTitleBox>
        </HeaderLeft>

        <CloseButton onClick={handleClose}>
          <CloseIcon fill={theme.colors.black} />
        </CloseButton>
      </Header>

      <NotesList>
        {notes.length === 0 ? (
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
          notes.map((note) => (
            <MemoCard
              key={note.id}
              id={note.id}
              content={note.content}
              memo={note.memo}
              handleDeleteMemo={handleDeleteMemo}
              handleUpdateMemo={handleUpdateMemo}
            />
          ))
        )}
      </NotesList>
    </Container>
  );
};

export default MemoPanel;

const Container = styled.aside<{ isOpen: boolean }>`
  position: fixed;
  top: 0;
  right: 0;
  z-index: 40;
  width: 342px;
  height: 100%;
  padding-top: 72px;
  border-left: 1px solid ${({ theme }) => theme.colors.stroke};

  display: flex;
  flex-direction: column;

  background-color: ${({ theme }) => theme.colors.white};

  transform: ${({ isOpen }) => (isOpen ? 'translateX(0)' : 'translateX(100%)')};
  transition: transform 0.3s;
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

  flex: 1;

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
