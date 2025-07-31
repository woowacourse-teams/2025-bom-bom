import styled from '@emotion/styled';
import { Dispatch, SetStateAction } from 'react';
import CommentIcon from '#/assets/comment.svg';
import DeleteIcon from '#/assets/delete.svg';

const Button = styled.button`
  border-radius: 9999px;

  display: inline-flex;
  align-items: center;
  justify-content: center;

  background: transparent;

  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #f5f5f5;
  }
`;

const Card = styled.div`
  overflow: hidden;
  position: relative;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  box-shadow: 0 1px 2px rgb(0 0 0 / 5%);

  background: #fff;

  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 4px 8px rgb(0 0 0 / 8%);
  }
`;

const Textarea = styled.textarea`
  width: 100%;
  min-height: 80px;
  padding: 1rem;
  outline: none;
  border: none;
  box-shadow: none;

  background: transparent;
  color: #111827;
  font-size: 0.875rem;

  resize: none;
`;

const NotesPanel = styled.aside<{ isOpen: boolean }>`
  position: fixed;
  top: 0;
  right: 0;
  z-index: 40;
  width: 24rem; /* w-96 */
  height: 100%;
  padding-top: 72px;
  border-left: 1px solid ${({ theme }) => theme.colors.stroke};

  background-color: ${({ theme }) => `${theme.colors.white}f8`};

  backdrop-filter: blur(24px);
  transform: ${({ isOpen }) => (isOpen ? 'translateX(0)' : 'translateX(100%)')};
  transition: transform 0.3s;
`;

const Header = styled.div`
  padding: 1.5rem; /* p-6 */
  border-bottom: 1px solid ${({ theme }) => theme.colors.dividers};

  display: flex;
  align-items: center;
  justify-content: space-between;

  backdrop-filter: blur(8px);
`;

const HeaderLeft = styled.div`
  display: flex;
  gap: 0.75rem; /* space-x-3 */
  align-items: center;
`;

const IconWrapper = styled.div`
  width: 2.5rem; /* w-10 */
  height: 2.5rem; /* h-10 */
  border-radius: 9999px;
  box-shadow: 0 1px 2px rgb(0 0 0 / 5%);

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => `${theme.colors.primary}15`};
`;

const NotesList = styled.div`
  padding: 1.5rem; /* p-6 */

  flex: 1;

  overflow-y: auto;
`;

const EmptyWrapper = styled.div`
  padding-top: 5rem; /* py-20 */
  padding-bottom: 5rem;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  h4 {
    margin-bottom: 0.5rem;
    color: ${({ theme }) => theme.colors.textSecondary};
  }

  p {
    color: ${({ theme }) => theme.colors.textTertiary};
    text-align: center;
  }
`;

const EmptyIconWrapper = styled.div`
  width: 4rem;
  height: 4rem;
  margin-bottom: 1rem;
  border-radius: 9999px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => `${theme.colors.primary}10`};
`;

const NoteCard = styled(Card)`
  overflow: hidden;
  position: relative;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  box-shadow: 0 1px 2px rgb(0 0 0 / 5%);

  background-color: ${({ theme }) => theme.colors.white};

  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 4px 8px rgb(0 0 0 / 8%);
  }
`;

const ColorDot = styled.div`
  position: absolute;
  top: 1rem;
  left: 1rem;
  z-index: 10;
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 9999px;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const DeleteButton = styled(Button)`
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  width: 1.5rem;
  height: 1.5rem;
  padding: 0;
  border-radius: 9999px;

  background-color: #fee2e2;
  color: #dc2626;

  opacity: 0;
  transition: opacity 0.2s;
`;

const NoteContent = styled.div`
  padding: 1rem;
  padding-left: 2rem;

  background-color: ${({ theme }) => theme.colors.disabledBackground};

  p {
    color: ${({ theme }) => theme.colors.textPrimary};
  }
`;

const NoteMemo = styled(Textarea)`
  min-height: 80px;
  padding: 1rem;
  outline: none;
  border: none;
  box-shadow: none;

  background: transparent;
  color: ${({ theme }) => theme.colors.textPrimary};

  resize: none;
`;

interface MemoPanelProps {
  open: boolean;
  notes: {
    id: string;
    content: string;
    memo: string;
  }[];
  handleDeleteNote: (id: string) => void;
  handleUpdateMemo: (id: string) => void;
  setOpen: Dispatch<SetStateAction<boolean>>;
}

export function MemoPanel({
  open,
  notes,
  handleDeleteNote,
  handleUpdateMemo,
  setOpen,
}: MemoPanelProps) {
  return (
    <NotesPanel isOpen={open}>
      <div className="flex flex-col h-full">
        <Header>
          <HeaderLeft>
            <IconWrapper>
              <CommentIcon />
            </IconWrapper>
            <div>
              <h3 className="theme-heading5">읽기 노트</h3>
              <p className="theme-caption">{notes.length}개의 메모</p>
            </div>
          </HeaderLeft>
          <Button
            onClick={() => setOpen(false)}
            className="w-8 h-8 p-0 rounded-full hover:bg-red-50"
          >
            <p>X</p>
          </Button>
        </Header>

        <NotesList>
          {notes.length === 0 ? (
            <EmptyWrapper>
              <EmptyIconWrapper>
                <CommentIcon />
              </EmptyIconWrapper>
              <h4 className="theme-body1 mb-2">아직 메모가 없어요</h4>
              <p className="theme-body2 text-center">
                중요한 내용을 메모로 기록해보세요
              </p>
            </EmptyWrapper>
          ) : (
            notes.map((note) => (
              <NoteCard key={note.id}>
                <ColorDot />
                <DeleteButton onClick={() => handleDeleteNote(note.id)}>
                  <DeleteIcon />
                </DeleteButton>
                <NoteContent>
                  <p className="theme-body2">{note.content}</p>
                </NoteContent>
                <NoteMemo
                  value={note.memo}
                  onChange={() => handleUpdateMemo(note.id)}
                  placeholder="메모하는 입력 공간"
                />
              </NoteCard>
            ))
          )}
        </NotesList>
      </div>
    </NotesPanel>
  );
}
