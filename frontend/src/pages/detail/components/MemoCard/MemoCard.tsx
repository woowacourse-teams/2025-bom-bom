import styled from '@emotion/styled';
import { ChangeEvent, useRef } from 'react';
import { theme } from '@/styles/theme';
import DeleteIcon from '#/assets/delete.svg';

interface MemoCardProps {
  id: string;
  content: string;
  memo: string;
  handleDeleteMemo: (id: string) => void;
  handleUpdateMemo: (id: string, e: ChangeEvent<HTMLTextAreaElement>) => void;
}

const MemoCard = ({
  id,
  content,
  memo,
  handleDeleteMemo,
  handleUpdateMemo,
}: MemoCardProps) => {
  const textAreaRef = useRef<HTMLTextAreaElement | null>(null);

  const autoResize = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const target = e.target;
    target.style.height = 'auto'; // 높이를 리셋
    target.style.height = `${target.scrollHeight}px`; // scrollHeight 만큼 늘림
    handleUpdateMemo(id, e);
  };

  return (
    <Container>
      <HeaderBox>
        <ColorDotWrapper>
          <ColorDot />
        </ColorDotWrapper>
        <DeleteButton onClick={() => handleDeleteMemo(id)}>
          <DeleteIcon fill={theme.colors.black} />
        </DeleteButton>
      </HeaderBox>

      <MemoContent>
        <MemoContentText>{content}</MemoContentText>
      </MemoContent>

      <NoteMemo
        ref={textAreaRef}
        rows={1}
        value={memo}
        onChange={autoResize}
        placeholder="메모를 입력해주세요"
      />
    </Container>
  );
};

export default MemoCard;

const Container = styled.div`
  width: 100%;
  padding: 12px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 12px;
  box-shadow: 0 1px 2px rgb(0 0 0 / 5%);

  display: flex;
  flex-direction: column;
  align-items: flex-start;
  align-self: stretch;

  background-color: ${({ theme }) => theme.colors.white};

  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 4px 8px rgb(0 0 0 / 8%);
  }
`;

const HeaderBox = styled.div`
  display: flex;
  align-items: center;
  align-self: stretch;
  justify-content: space-between;
`;

const ColorDotWrapper = styled.div`
  padding: 6px;

  display: flex;
  flex-direction: column;
`;

const ColorDot = styled.div`
  width: 12px;
  height: 12px;
  border-radius: 50%;

  background-color: ${({ theme }) => theme.colors.primary};

  aspect-ratio: 1/1;
`;

const DeleteButton = styled.button`
  padding: 4px;
`;

const MemoContent = styled.div`
  width: 100%;
  padding: 4px 8px;
  border-radius: 8px;

  background-color: ${({ theme }) => theme.colors.disabledBackground};
`;

const MemoContentText = styled.p`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const NoteMemo = styled.textarea`
  width: 100%;
  padding: 8px 12px;
  outline: none;
  border: none;
  box-shadow: none;

  display: flex;
  flex-direction: column;
  align-items: flex-start;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};

  resize: none;
`;
