import styled from '@emotion/styled';
import { ElementType, useEffect, useRef, useState } from 'react';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';
import { theme } from '@/styles/theme';
import DeleteIcon from '#/assets/delete.svg';

interface MemoCardProps {
  id: number;
  content: string;
  memo: string;
  newsletterName?: string;
  newsletterImageUrl?: string;
  articleTitle?: string;
  createdAt?: string;
  as?: ElementType;
  onClick?: () => void;
  onRemoveButtonClick?: (id: number) => void;
  onMemoChange?: (id: number, memo: string) => void; // <-- e 대신 memo string
}

const MemoCard = ({
  id,
  content,
  memo,
  newsletterName,
  newsletterImageUrl,
  articleTitle,
  createdAt,
  as,
  onClick,
  onRemoveButtonClick,
  onMemoChange,
}: MemoCardProps) => {
  const textAreaRef = useRef<HTMLTextAreaElement | null>(null);
  const [localMemo, setLocalMemo] = useState(memo);
  const debouncedMemo = useDebouncedValue(localMemo, 500);

  const handleRemoveButtonClick = () => {
    onRemoveButtonClick?.(id);
  };

  useEffect(() => {
    if (debouncedMemo !== memo) {
      onMemoChange?.(id, debouncedMemo);
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
    <Container as={as} onClick={onClick}>
      <HeaderBox>
        {articleTitle && newsletterName && newsletterImageUrl && (
          <NewsletterInfo>
            <ArticleTitle>{articleTitle}</ArticleTitle>
            <NewsletterMeta>
              <NewsletterImage src={newsletterImageUrl} alt={newsletterName} />
              <NewsletterName>{newsletterName}</NewsletterName>
            </NewsletterMeta>
          </NewsletterInfo>
        )}
      </HeaderBox>

      <MemoContent>
        <MemoContentText>{content}</MemoContentText>
      </MemoContent>

      <MemoFooter>
        {onMemoChange ? (
          <NoteMemo
            ref={textAreaRef}
            name="memo"
            rows={1}
            value={localMemo}
            onChange={(e) => setLocalMemo(e.target.value)}
            placeholder="메모를 입력해주세요"
          />
        ) : (
          <MemoText>{memo || '메모가 없습니다.'}</MemoText>
        )}
      </MemoFooter>
      {createdAt && (
        <CreatedAtText>
          {new Date(createdAt).toLocaleDateString('ko-KR')}
        </CreatedAtText>
      )}

      {onRemoveButtonClick && (
        <DeleteButton onClick={handleRemoveButtonClick}>
          <DeleteIcon fill={theme.colors.black} width={20} height={20} />
        </DeleteButton>
      )}
    </Container>
  );
};

export default MemoCard;

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

const HeaderBox = styled.div`
  display: flex;
  align-items: flex-start;
  align-self: stretch;
  justify-content: space-between;
`;

const NewsletterInfo = styled.div`
  display: flex;
  gap: 12px;
  flex: 1;
  flex-direction: column;
  align-items: flex-start;
`;

const NewsletterImage = styled.img`
  width: 28px;
  height: 28px;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgb(0 0 0 / 10%);

  object-fit: cover;
`;

const NewsletterMeta = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
`;

const NewsletterName = styled.span`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const ArticleTitle = styled.h3`
  margin: 0;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body1};
  font-weight: 600;
  line-height: 1.4;
`;

const DeleteButton = styled.button`
  position: absolute;
  right: 16px;
  bottom: 16px;
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

const MemoFooter = styled.div`
  width: 100%;

  display: flex;
  gap: 12px;
  flex-direction: column;
`;

const CreatedAtText = styled.time`
  margin-top: 4px;

  align-self: flex-end;

  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
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
  line-height: 1.6;

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
  line-height: 1.5;

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

const MemoText = styled.p`
  width: 100%;
  margin: 0;
  padding: 16px;
  border: 2px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 12px;

  background-color: ${({ theme }) => theme.colors.white};
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body2};
  line-height: 1.5;
  white-space: pre-wrap;

  transition: all 0.2s ease-in-out;
  word-break: break-all;
`;
