import styled from '@emotion/styled';
import { formatDate } from '@/utils/date';
import type { Highlight } from '../../types/highlight';
import type { ElementType } from 'react';

const DELETE_MEMO_ARTICLE_ID = 0;

interface ReadOnlyMemoCardProps {
  data: Highlight;
  as?: ElementType;
  onClick?: () => void;
}

const ReadOnlyMemoCard = ({ data, as, onClick }: ReadOnlyMemoCardProps) => {
  const {
    text,
    memo,
    newsletterName,
    newsletterImageUrl,
    articleId,
    articleTitle,
    createdAt,
  } = data;
  const isDeleted = articleId === DELETE_MEMO_ARTICLE_ID;

  return (
    <Container as={as} onClick={onClick} disabled={isDeleted}>
      <HeaderBox>
        <ArticleTitle disabled={isDeleted}>
          {articleTitle}
          {isDeleted && <DeleteText>(삭제됨)</DeleteText>}
        </ArticleTitle>
      </HeaderBox>

      <MemoContent>
        <MemoContentText>{text}</MemoContentText>
      </MemoContent>

      <MemoText>{memo || '메모가 없습니다.'}</MemoText>

      <MemoFooter>
        <NewsletterMeta>
          <NewsletterImage src={newsletterImageUrl} alt={newsletterName} />
          <NewsletterName>{newsletterName}</NewsletterName>
        </NewsletterMeta>
        <CreatedAtText>
          {formatDate(new Date(createdAt ?? ''), '. ')}
        </CreatedAtText>
      </MemoFooter>
    </Container>
  );
};

export default ReadOnlyMemoCard;

const Container = styled.div<{ disabled: boolean }>`
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

  &:disabled {
    box-shadow: none;

    background-color: ${({ theme }) => theme.colors.disabledBackground};

    border-color: transparent;
    transform: none;
  }
`;

const HeaderBox = styled.div`
  display: flex;
  align-items: flex-start;
  align-self: stretch;
  justify-content: space-between;
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

const ArticleTitle = styled.h3<{ disabled: boolean }>`
  margin: 0;

  color: ${({ theme, disabled }) =>
    disabled ? theme.colors.disabledText : theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body1};
  font-weight: 600;
`;

const DeleteText = styled.span`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
  white-space: nowrap;
`;

const MemoFooter = styled.div`
  width: 100%;

  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
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

  word-break: break-all;
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
  white-space: pre-wrap;

  transition: all 0.2s ease-in-out;
  word-break: break-all;
`;
