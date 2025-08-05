import styled from '@emotion/styled';
import { useEffect, useRef, useState } from 'react';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';
import { theme } from '@/styles/theme';
import DeleteIcon from '#/assets/delete.svg';

interface MemoCardProps {
  id: number;
  content: string;
  memo: string;
  onRemoveButtonClick: (id: number) => void;
  onMemoChange: (id: number, memo: string) => void; // <-- e 대신 memo string
}

const MemoCard = ({
  id,
  content,
  memo,
  onRemoveButtonClick,
  onMemoChange,
}: MemoCardProps) => {
  const textAreaRef = useRef<HTMLTextAreaElement | null>(null);
  const [localMemo, setLocalMemo] = useState(memo);
  const debouncedMemo = useDebouncedValue(localMemo, 500);

  const handleRemoveButtonClick = () => {
    onRemoveButtonClick(id);
  };

  useEffect(() => {
    if (debouncedMemo !== memo) {
      onMemoChange(id, debouncedMemo);
    }
    // 의존성 배열에 updateMemo, id, memo를 넣으면
    // memo가 변경될 때마다(예: 서버 응답으로 값이 업데이트될 때) 불필요하게 effect가 재실행됨.
    // 우리는 debouncedMemo가 변경될 때만 서버 업데이트를 호출하려는 목적이므로 eslint 경고를 무시하고 의존성 배열을 최소화함.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedMemo]);

  useEffect(() => {
    if (!textAreaRef.current) return;

    const el = textAreaRef.current;
    el.style.height = 'auto';
    el.style.height = `${el.scrollHeight}px`;
  }, [localMemo]);

  return (
    <Container>
      <HeaderBox>
        <ColorDotWrapper>
          <ColorDot />
        </ColorDotWrapper>
        <DeleteButton onClick={handleRemoveButtonClick}>
          <DeleteIcon fill={theme.colors.black} width={20} height={20} />
        </DeleteButton>
      </HeaderBox>

      <MemoContent>
        <MemoContentText>{content}</MemoContentText>
      </MemoContent>

      <NoteMemo
        ref={textAreaRef}
        rows={1}
        value={localMemo}
        onChange={(e) => setLocalMemo(e.target.value)}
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
  height: auto;
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
