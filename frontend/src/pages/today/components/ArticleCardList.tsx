import styled from '@emotion/styled';
import ArticleCard from './ArticleCard';
import checkIcon from '../../../../public/assets/check.svg';
import letterIcon from '../../../../public/assets/letter.svg';

function ArticleCardList() {
  return (
    <Container>
      <ListTitleBox>
        <img src={letterIcon} alt="새로운 뉴스레터 타이틀 이미지" />
        <ListTitle>새로운 뉴스레터 (2개)</ListTitle>
      </ListTitleBox>
      <CardList>
        <ArticleCard
          data={{
            articleId: '1',
            title: '폭염에도 전력난 없는 이유는?',
            contentsSummary:
              '자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간 자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간',
            arrivedDateTime: new Date('2025.07.01'),
            thumbnailUrl:
              'https://img.freepik.com/free-vector/illustration-south-korea-flag_53876-27132.jpg',
            expectedReadTime: 5,
            isRead: false,
            newsletter: {
              category: '기술',
              name: 'UPPITY',
              imageUrl: 'https://example.com/newsletter-image.jpg',
            },
          }}
        />
        <ArticleCard
          data={{
            articleId: '1',
            title: '폭염에도 전력난 없는 이유는?',
            contentsSummary:
              '자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간 자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간',
            arrivedDateTime: new Date('2025.07.01'),
            thumbnailUrl:
              'https://img.freepik.com/free-vector/illustration-south-korea-flag_53876-27132.jpg',
            expectedReadTime: 5,
            isRead: false,
            newsletter: {
              category: '기술',
              name: 'UPPITY',
              imageUrl: 'https://example.com/newsletter-image.jpg',
            },
          }}
        />
      </CardList>
      <ListTitleBox>
        <img src={checkIcon} alt="새로운 뉴스레터 타이틀 이미지" />
        <ListTitle>읽은 뉴스레터 (1개)</ListTitle>
      </ListTitleBox>
      <CardList>
        <ArticleCard
          data={{
            articleId: '1',
            title: '폭염에도 전력난 없는 이유는?',
            contentsSummary:
              '자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간 자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간',
            arrivedDateTime: new Date('2025.07.01'),
            thumbnailUrl:
              'https://img.freepik.com/free-vector/illustration-south-korea-flag_53876-27132.jpg',
            expectedReadTime: 5,
            isRead: true,
            newsletter: {
              category: '기술',
              name: 'UPPITY',
              imageUrl: 'https://example.com/newsletter-image.jpg',
            },
          }}
        />
      </CardList>
    </Container>
  );
}

export default ArticleCardList;

const Container = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  gap: 16px;
`;

const ListTitleBox = styled.div`
  display: flex;
  align-items: center;

  gap: 12px;
`;

const ListTitle = styled.h5`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const CardList = styled.ul`
  display: flex;
  flex-direction: column;

  gap: 16px;
`;
