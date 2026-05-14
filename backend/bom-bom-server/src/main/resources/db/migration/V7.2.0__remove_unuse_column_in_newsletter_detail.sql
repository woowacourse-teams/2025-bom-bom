-- newsletter_detail 테이블에서 사용하지 않는 컬럼 제거

-- subscribePageImageUrl 컬럼 삭제
ALTER TABLE `newsletter_detail` DROP COLUMN `subscribePageImageUrl`;

-- subscribe_page_image_url 컬럼 삭제  
ALTER TABLE `newsletter_detail` DROP COLUMN `subscribe_page_image_url`;

-- previousNewsletterUrl 컬럼 삭제
ALTER TABLE `newsletter_detail` DROP COLUMN `previousNewsletterUrl`;
