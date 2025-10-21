import { theme } from '@bombom/shared/theme';
import styled from '@emotion/styled';
import { useState } from 'react';
import ArticleCard from '../ArticleCard/ArticleCard';
import EmptyLetterCard from '../EmptyLetterCard/EmptyLetterCard';
import useModal from '@/components/Modal/useModal';
import { useDevice } from '@/hooks/useDevice';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import ArticleDeleteModal from '@/pages/storage/components/ArticleDeleteModal/ArticleDeleteModal';
import type { Article } from '@/types/articles';
import CheckIcon from '#/assets/svg/check.svg';
import LetterIcon from '#/assets/svg/letter.svg';

type ExtendedArticle = Article & {
  type: 'guide' | 'article';
};

interface ArticleCardListProps {
  articles: ExtendedArticle[];
  onDeleteArticles?: (articleIds: number[]) => void;
}

const ArticleCardList = ({
  articles,
  onDeleteArticles,
}: ArticleCardListProps) => {
  const device = useDevice();
  const isMobile = device === 'mobile';
  const [pendingDeleteArticle, setPendingDeleteArticle] =
    useState<ExtendedArticle | null>(null);
  const { modalRef, isOpen, openModal, closeModal } = useModal();

  const grouped = articles.reduce<{
    read: ExtendedArticle[];
    unread: ExtendedArticle[];
  }>(
    (acc, article) => {
      if (article.isRead) acc.read.push(article);
      else acc.unread.push(article);
      return acc;
    },
    { read: [], unread: [] },
  );

  const hasBookmarkedArticles = pendingDeleteArticle?.isBookmarked ?? false;

  const handleDeleteClick = (article: ExtendedArticle) => {
    setPendingDeleteArticle(article);
    openModal();
  };

  const handleConfirmDelete = () => {
    if (pendingDeleteArticle) {
      onDeleteArticles?.([pendingDeleteArticle?.articleId]);
      setPendingDeleteArticle(null);
    }
  };

  if (articles.length === 0)
    return <EmptyLetterCard title="새로운 뉴스레터가 없어요" />;

  return (
    <Container isMobile={isMobile}>
      <LettersWrapper isMobile={isMobile}>
        <ListTitleBox>
          <LetterIcon width={32} height={32} color={theme.colors.white} />
          <ListTitle>새로운 뉴스레터 ({grouped.unread.length}개)</ListTitle>
        </ListTitleBox>
        <CardList isMobile={isMobile}>
          {grouped.unread.map((article) => (
            <li key={article.articleId}>
              <ArticleCard
                data={article}
                to={
                  article.type === 'guide'
                    ? `/articles/guide/${article.articleId}`
                    : `/articles/${article.articleId}`
                }
                onClick={() => {
                  trackEvent({
                    category: 'Article',
                    action:
                      article.type === 'guide'
                        ? '가이드 메일 클릭'
                        : '아티클 클릭',
                    label: article.title ?? 'Unknown Article',
                  });
                }}
                onDelete={() => handleDeleteClick(article)}
              />
            </li>
          ))}
        </CardList>
      </LettersWrapper>
      <LettersWrapper isMobile={isMobile}>
        {grouped.read.length > 0 && (
          <ListTitleBox>
            <CheckIcon width={32} height={32} color={theme.colors.black} />
            <ListTitle>읽은 뉴스레터 ({grouped.read.length}개)</ListTitle>
          </ListTitleBox>
        )}
        <CardList isMobile={isMobile}>
          {grouped.read.map((article) => (
            <li key={article.articleId}>
              <ArticleCard
                data={article}
                to={
                  article.type === 'guide'
                    ? `/articles/guide/${article.articleId}`
                    : `/articles/${article.articleId}`
                }
                onClick={() => {
                  trackEvent({
                    category: 'Article',
                    action:
                      article.type === 'guide'
                        ? '가이드 메일 클릭'
                        : '아티클 클릭',
                    label: article.title ?? 'Unknown Article',
                  });
                }}
                onDelete={() => handleDeleteClick(article)}
              />
            </li>
          ))}
        </CardList>
      </LettersWrapper>
      <ArticleDeleteModal
        modalRef={modalRef}
        isOpen={isOpen}
        closeModal={closeModal}
        onDelete={handleConfirmDelete}
        hasBookmarkedArticles={hasBookmarkedArticles}
      />
    </Container>
  );
};

export default ArticleCardList;

export const Container = styled.div<{ isMobile: boolean }>`
  width: 100%;

  display: flex;
  gap: 24px;
  flex-direction: column;
  align-items: flex-start;
`;

export const LettersWrapper = styled.div<{ isMobile: boolean }>`
  width: 100%;

  display: flex;
  gap: 8px;
  flex-direction: column;
`;

const ListTitleBox = styled.div`
  display: flex;
  gap: 12px;
  align-items: center;
`;

const ListTitle = styled.h5`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
`;

export const CardList = styled.ul<{ isMobile: boolean }>`
  width: 100%;

  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '0' : '16px')};
  flex-direction: column;

  ${({ isMobile, theme }) =>
    isMobile &&
    `
    li {
      padding: 8px 0;
    }
    li:not(:last-child) {
      border-bottom: 2px solid ${theme.colors.dividers};
    }
  `}
`;
