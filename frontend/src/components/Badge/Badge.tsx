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
  align-items: center;
  justify-content: center;

  width: fit-content;
  padding: 4px 8px;
  border-radius: 8px;

  background-color: ${({ theme }) => theme.colors.dividers};

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.caption};
`;
