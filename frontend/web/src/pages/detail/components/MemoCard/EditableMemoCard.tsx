import styled from '@emotion/styled';
import { useEffect, useRef, useState } from 'react';
import { theme } from 'shared';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import DeleteIcon from '#/assets/svg/delete.svg';

interface EditableMemoCardProps {
  id: number;
  content: string;
  memo?: string;
  onRemoveButtonClick: ({ id }: { id: number }) => void;
  onMemoChange: (id: number, memo: string) => void;
}

const EditableMemoCard = ({
  id,
  content,
  memo,
  onRemoveButtonClick,
  onMemoChange,
}: EditableMemoCardProps) => {
  const textAreaRef = useRef<HTMLTextAreaElement | null>(null);
  const [localMemo, setLocalMemo] = useState(memo);
  const debouncedMemo = useDebouncedValue(localMemo, 500);

  const handleRemoveButtonClick = () => {
    onRemoveButtonClick({ id });
    trackEvent({
      category: 'Memo',
      action: '메모 패널 - 하이라이트 삭제',
      label: '아티클 본문',
    });
  };

  useEffect(() => {
    if (debouncedMemo !== memo) {
      onMemoChange(id, debouncedMemo ?? '');
      trackEvent({
        category: 'Memo',
        action: '메모 패널 - 메모 수정',
        label: '아티클 본문',
      });
    }
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
      <MemoContent>
        <MemoContentText>{content}</MemoContentText>
      </MemoContent>

      <NoteMemo
        ref={textAreaRef}
        name="memo"
        rows={1}
        value={localMemo}
        onChange={(e) => setLocalMemo(e.target.value)}
        placeholder="메모를 입력해주세요"
      />

      <DeleteButton onClick={handleRemoveButtonClick}>
        <DeleteIcon fill={theme.colors.black} width={20} height={20} />
      </DeleteButton>
    </Container>
  );
};

export default EditableMemoCard;

const Container = styled.div`
  position: relative;
  width: 100%;
  padding: 20px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 16px;
  box-shadow: 0 2px 8px rgb(0 0 0 / 4%);

  display: flex;
  gap: 16px;
  flex-direction: column;
  align-items: flex-start;
  align-self: stretch;

  background-color: ${({ theme }) => theme.colors.white};
  text-align: left;

  transition: all 0.2s ease-in-out;

  &:hover {
    box-shadow: 0 8px 24px rgb(0 0 0 / 8%);

    border-color: ${({ theme }) => theme.colors.primary};
    transform: translateY(-1px);
  }
`;

const DeleteButton = styled.button`
  margin-left: auto;
  padding: 8px;
  border: none;
  border-radius: 8px;

  background-color: transparent;
  color: ${({ theme }) => theme.colors.textSecondary};

  cursor: pointer;
  transition: all 0.2s ease-in-out;

  &:hover {
    background-color: ${({ theme }) => theme.colors.primary};
    color: ${({ theme }) => theme.colors.white};

    transform: scale(1.1);
  }
`;

const MemoContent = styled.div`
  width: 100%;
  padding: 16px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 12px;

  background: linear-gradient(
    135deg,
    ${({ theme }) => theme.colors.disabledBackground} 0%,
    ${({ theme }) => theme.colors.white} 100%
  );

  transition: all 0.2s ease-in-out;
`;

const MemoContentText = styled.p`
  margin: 0;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};

  word-break: break-all;
`;

const NoteMemo = styled.textarea`
  width: 100%;
  height: auto;
  padding: 16px;
  outline: none;
  border: 2px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 12px;
  box-shadow: none;

  background-color: ${({ theme }) => theme.colors.white};
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};

  resize: none;
  transition: all 0.2s ease-in-out;

  &:focus {
    box-shadow: 0 0 0 3px ${({ theme }) => theme.colors.primary}20;
    border-color: ${({ theme }) => theme.colors.primary};
  }

  &::placeholder {
    color: ${({ theme }) => theme.colors.textTertiary};
  }
`;
