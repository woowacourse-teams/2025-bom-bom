-- V1__init_schema.sql

-- Member
CREATE TABLE member (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        provider VARCHAR(255) NOT NULL,
                        provider_id VARCHAR(255) NOT NULL,
                        email VARCHAR(30) NOT NULL,
                        nickname VARCHAR(255) NOT NULL UNIQUE,
                        profile_image_url VARCHAR(512),
                        birth_date DATE,
                        gender VARCHAR(255) NOT NULL,
                        role_id BIGINT NOT NULL,
                        deleted_at DATETIME(6),
                        created_at DATETIME(6) NOT NULL,
                        updated_at DATETIME(6) NOT NULL,
                        UNIQUE (provider, provider_id)
);

-- Role
CREATE TABLE role (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      authority VARCHAR(20) NOT NULL
);

-- Article
CREATE TABLE article (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         title VARCHAR(255) NOT NULL,
                         contents MEDIUMTEXT NOT NULL,
                         thumbnail_url VARCHAR(512),
                         expected_read_time TINYINT,
                         contents_summary VARCHAR(255) NOT NULL,
                         is_read BOOLEAN DEFAULT FALSE,
                         member_id BIGINT NOT NULL,
                         newsletter_id BIGINT NOT NULL,
                         arrived_date_time DATETIME(6) NOT NULL,
                         created_at DATETIME(6) NOT NULL,
                         updated_at DATETIME(6) NOT NULL
);

-- Bookmark
CREATE TABLE bookmark (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          article_id BIGINT NOT NULL,
                          member_id BIGINT NOT NULL,
                          created_at DATETIME(6) NOT NULL,
                          updated_at DATETIME(6) NOT NULL,
                          UNIQUE (member_id, article_id)
);

-- Highlight
CREATE TABLE highlight (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           start_offset INT,
                           start_x_path VARCHAR(255),
                           end_offset INT,
                           end_x_path VARCHAR(255),
                           article_id BIGINT NOT NULL,
                           color VARCHAR(10) NOT NULL,
                           highlight_text TEXT NOT NULL,
                           memo VARCHAR(500),
                           created_at DATETIME(6) NOT NULL,
                           updated_at DATETIME(6) NOT NULL
);

-- Newsletter
CREATE TABLE newsletter (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            description VARCHAR(255) NOT NULL,
                            image_url VARCHAR(512) NOT NULL,
                            email VARCHAR(60) NOT NULL,
                            category_id BIGINT NOT NULL,
                            detail_id BIGINT NOT NULL,
                            created_at DATETIME(6) NOT NULL,
                            updated_at DATETIME(6) NOT NULL
);

-- Category
CREATE TABLE category (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(20) NOT NULL
);

-- NewsletterDetail
CREATE TABLE newsletter_detail (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   main_page_url VARCHAR(512) NOT NULL,
                                   subscribe_url VARCHAR(512) NOT NULL,
                                   issue_cycle VARCHAR(255) NOT NULL,
                                   subscribe_count BIGINT NOT NULL,
                                   sender VARCHAR(100) NOT NULL,
                                   subscribe_page_image_url VARCHAR(512),
                                   previous_newsletter_url VARCHAR(512)
);

-- Subscribe
CREATE TABLE subscribe (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           newsletter_id BIGINT NOT NULL,
                           member_id BIGINT NOT NULL,
                           created_at DATETIME(6) NOT NULL,
                           updated_at DATETIME(6) NOT NULL
);

-- Pet
CREATE TABLE pet (
                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                     member_id BIGINT NOT NULL,
                     stage_id BIGINT NOT NULL,
                     current_score INT DEFAULT 0 NOT NULL,
                     is_attended BOOLEAN DEFAULT FALSE NOT NULL,
                     created_at DATETIME(6) NOT NULL,
                     updated_at DATETIME(6) NOT NULL
);

-- Stage
CREATE TABLE stage (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       level INT NOT NULL,
                       required_score INT NOT NULL,
                       created_at DATETIME(6) NOT NULL,
                       updated_at DATETIME(6) NOT NULL
);

-- ContinueReading
CREATE TABLE continue_reading (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  member_id BIGINT NOT NULL UNIQUE,
                                  day_count SMALLINT NOT NULL,
                                  created_at DATETIME(6) NOT NULL,
                                  updated_at DATETIME(6) NOT NULL
);

-- TodayReading
CREATE TABLE today_reading (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               member_id BIGINT NOT NULL UNIQUE,
                               total_count TINYINT NOT NULL,
                               current_count TINYINT NOT NULL,
                               created_at DATETIME(6) NOT NULL,
                               updated_at DATETIME(6) NOT NULL
);

-- WeeklyReading
CREATE TABLE weekly_reading (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                member_id BIGINT NOT NULL UNIQUE,
                                goal_count TINYINT NOT NULL,
                                current_count TINYINT NOT NULL,
                                created_at DATETIME(6) NOT NULL,
                                updated_at DATETIME(6) NOT NULL
);

-- MonthlyReading
CREATE TABLE monthly_reading (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 member_id BIGINT NOT NULL UNIQUE,
                                 current_count SMALLINT NOT NULL
);

-- YearlyReading
CREATE TABLE yearly_reading (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                member_id BIGINT NOT NULL,
                                current_count SMALLINT NOT NULL,
                                reading_year INT NOT NULL,
                                UNIQUE (member_id, reading_year)
);
