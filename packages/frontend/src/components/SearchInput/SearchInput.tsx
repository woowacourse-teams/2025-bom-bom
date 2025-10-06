import styled from '@emotion/styled';
import { theme } from '@/styles/theme';
import type { ComponentProps } from 'react';
import ReadingGlassesIcon from '#/assets/svg/reading-glasses.svg';

export default function SearchInput({ ...props }: ComponentProps<'input'>) {
  return (
    <Container>
      <ReadingGlassesIconWrapper>
        <ReadingGlassesIcon
          color={theme.colors.textTertiary}
          width={16}
          height={16}
        />
      </ReadingGlassesIconWrapper>
      <StyledInput
        type="search"
        aria-label={props['aria-label'] || '검색'}
        {...props}
      />
    </Container>
  );
}

const Container = styled.div`
  position: relative;
  width: 100%;
  height: 42px;
`;

const ReadingGlassesIconWrapper = styled.div`
  position: absolute;
  top: 50%;
  left: 12px;

  transform: translateY(-50%);
`;

const StyledInput = styled.input`
  width: 100%;
  height: 100%;
  padding: 12px 42px;
  outline: none;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 14px;
  box-shadow:
    0 1px 3px 0 rgb(0 0 0 / 10%),
    0 1px 2px -1px rgb(0 0 0 / 10%);

  background-color: ${({ theme }) => theme.colors.white};
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body1};

  transition: all 0.2s ease-in-out;

  &::placeholder {
    color: ${({ theme }) => theme.colors.textTertiary};
  }

  &:focus {
    box-shadow:
      0 1px 3px 0 rgb(0 0 0 / 10%),
      0 1px 2px -1px rgb(0 0 0 / 10%),
      0 0 0 2px ${({ theme }) => theme.colors.primary}20;
    border-color: ${({ theme }) => theme.colors.primary};
  }

  &:disabled {
    background-color: ${({ theme }) => theme.colors.disabledBackground};
    color: ${({ theme }) => theme.colors.textTertiary};

    cursor: not-allowed;
  }
`;
