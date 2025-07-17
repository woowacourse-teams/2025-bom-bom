import styled from '@emotion/styled';

interface BadgeProps {
  text: string;
}

function Badge({ text }: BadgeProps) {
  return <Container>{text}</Container>;
}

export default Badge;

const Container = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;

  width: fit-content;
  padding: 4px 8px;

  ${({ theme }) => theme.fonts.caption};

  color: ${({ theme }) => theme.colors.textPrimary};
  background-color: ${({ theme }) => theme.colors.dividers};
  border-radius: 8px;
`;
