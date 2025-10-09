import styled from '@emotion/styled';

export const Container = styled.div<{ size: number }>`
  height: ${({ size }) => size}px;
`;

export default function Spacing({ size }: { size: number }) {
  return <Container size={size} />;
}
