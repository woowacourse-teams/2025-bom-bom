-- 블로그 글
CREATE TABLE blog_post (
                           id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                           member_id BIGINT NOT NULL,
                           title VARCHAR(200) NULL,
                           content LONGTEXT NULL,
                           thumbnail_image_id BIGINT NULL,
                           status ENUM('DRAFT', 'PUBLISHED', 'DELETED') NOT NULL,
                           visibility ENUM('PRIVATE', 'PUBLIC') NOT NULL,
                           category_id BIGINT NULL,
                           expected_read_time INT NULL,
                           published_at DATETIME(6) NULL,
                           created_at DATETIME(6) NOT NULL,
                           updated_at DATETIME(6) NOT NULL,
                           KEY idx_blog_post_status_visibility_published_at (status, visibility, published_at),
                           KEY idx_blog_post_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 블로그 카테고리
CREATE TABLE blog_category (
                               id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                               name VARCHAR(50) NOT NULL,
                               created_at DATETIME(6) NOT NULL,
                               updated_at DATETIME(6) NOT NULL,
                               CONSTRAINT uk_blog_category_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 블로그 해시태그
CREATE TABLE blog_hashtag (
                               id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                               name VARCHAR(50) NOT NULL,
                               created_at DATETIME(6) NOT NULL,
                               updated_at DATETIME(6) NOT NULL,
                               CONSTRAINT uk_blog_hashtag_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 블로그 해시태그 연관관계
CREATE TABLE blog_post_tag (
                               id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                               blog_post_id BIGINT NOT NULL,
                               blog_hashtag_id BIGINT NOT NULL,
                               created_at DATETIME(6) NOT NULL,
                               updated_at DATETIME(6) NOT NULL,
                               CONSTRAINT uk_blog_post_tag_post_tag UNIQUE (blog_post_id, blog_hashtag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 블로그 이미지
CREATE TABLE blog_image_asset (
                                  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                  blog_post_id BIGINT NOT NULL,
                                  object_key VARCHAR(500) NOT NULL,
                                  image_url VARCHAR(1000) NOT NULL,
                                  status ENUM('UPLOADED', 'ATTACHED', 'DELETE_PENDING') NOT NULL,
                                  delete_requested_at DATETIME(6) NULL,
                                  created_at DATETIME(6) NOT NULL,
                                  updated_at DATETIME(6) NOT NULL,
                                  CONSTRAINT uk_blog_image_asset_object_key UNIQUE (object_key),
                                  KEY idx_blog_image_asset_blog_post_status (blog_post_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
