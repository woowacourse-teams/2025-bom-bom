import styled from '@emotion/styled';
import PostboxIcon from '@/components/icons/postboxIcon';

function EmptyLetterCard() {
  return (
    <Container>
      <PostboxIcon />
    </Container>
  );
}

export default EmptyLetterCard;

const Container = styled.section`
  width: 100%;
`;
