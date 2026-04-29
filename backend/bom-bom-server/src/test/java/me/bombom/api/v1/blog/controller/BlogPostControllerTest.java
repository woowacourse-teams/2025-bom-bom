package me.bombom.api.v1.blog.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.blog.domain.BlogCategory;
import me.bombom.api.v1.blog.domain.BlogHashtag;
import me.bombom.api.v1.blog.domain.BlogImageAsset;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogPostVisibility;
import me.bombom.api.v1.blog.repository.BlogCategoryRepository;
import me.bombom.api.v1.blog.repository.BlogHashtagRepository;
import me.bombom.api.v1.blog.repository.BlogImageAssetRepository;
import me.bombom.api.v1.blog.repository.BlogPostRepository;
import me.bombom.api.v1.blog.repository.BlogPostTagRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class BlogPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogCategoryRepository blogCategoryRepository;

    @Autowired
    private BlogImageAssetRepository blogImageAssetRepository;

    @Autowired
    private BlogHashtagRepository blogHashtagRepository;

    @Autowired
    private BlogPostTagRepository blogPostTagRepository;

    private BlogPost publicPost;

    @BeforeEach
    void setUp() {
        blogPostTagRepository.deleteAllInBatch();
        blogHashtagRepository.deleteAllInBatch();
        blogImageAssetRepository.deleteAllInBatch();
        blogPostRepository.deleteAllInBatch();
        blogCategoryRepository.deleteAllInBatch();

        BlogCategory category = blogCategoryRepository.save(TestFixture.createBlogCategory("뉴스레터 추천"));
        publicPost = blogPostRepository.save(TestFixture.createBlogPost(
                1L,
                "왜 다들 뉴스레터를 읽을까?",
                "본문 내용",
                null,
                category.getId(),
                BlogPostStatus.PUBLISHED,
                BlogPostVisibility.PUBLIC,
                LocalDateTime.of(2026, 4, 15, 10, 0)
        ));

        BlogImageAsset thumbnail = blogImageAssetRepository.save(
                TestFixture.createBlogImageAsset(publicPost.getId(), "blog/summary-thumb", "https://cdn.bombom.me/summary.png")
        );
        ReflectionTestUtils.setField(publicPost, "thumbnailImageId", thumbnail.getId());
        blogPostRepository.save(publicPost);

        BlogHashtag firstHashtag = blogHashtagRepository.save(TestFixture.createBlogHashtag("뉴스레터"));
        BlogHashtag secondHashtag = blogHashtagRepository.save(TestFixture.createBlogHashtag("생산성"));
        blogPostTagRepository.save(TestFixture.createBlogPostTag(publicPost.getId(), firstHashtag.getId()));
        blogPostTagRepository.save(TestFixture.createBlogPostTag(publicPost.getId(), secondHashtag.getId()));
    }

    @Test
    void 익명_사용자가_공개_발행_블로그_요약을_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/blog/posts/{postId}/summary", publicPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(publicPost.getId()))
                .andExpect(jsonPath("$.title").value("왜 다들 뉴스레터를 읽을까?"))
                .andExpect(jsonPath("$.description").value("왜 다들 뉴스레터를 읽을까? 설명"))
                .andExpect(jsonPath("$.thumbnailImageUrl").value("https://cdn.bombom.me/summary.png"))
                .andExpect(jsonPath("$.categoryName").value("뉴스레터 추천"))
                .andExpect(jsonPath("$.hashtags").isArray())
                .andExpect(jsonPath("$.hashtags.length()").value(2));
    }
}
