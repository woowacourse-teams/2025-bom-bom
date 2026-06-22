package me.bombom.api.v1.blog.controller;

import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AdditionalAcceptanceDataSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@AcceptanceTest("acceptance/blog/blog-post.json")
class BlogPostControllerTest {

    private static final long ADMIN_MEMBER_ID = 102L;
    private static final long PUBLIC_POST_ID = 1L;
    private static final long PRIVATE_POST_ID = 2L;
    private static final long DRAFT_POST_ID = 3L;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 익명_사용자는_공개된_블로그_목록만_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/blog/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("공개 글"))
                .andExpect(jsonPath("$.content[0].thumbnailImageUrl").value("https://cdn.bombom.me/public.png"));
    }

    @Test
    void 관리자는_비공개_블로그를_목록에서_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/blog/posts").header(MEMBER_ID, ADMIN_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value("공개 글"))
                .andExpect(jsonPath("$.content[1].title").value("비공개 글"));
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/blog/paging.json")
    void 블로그_목록에_페이징을_적용한다() throws Exception {
        mockMvc.perform(get("/api/v1/blog/posts").queryParam("page", "0").queryParam("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void 익명_사용자가_공개_블로그_상세를_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/blog/posts/{postId}", PUBLIC_POST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("공개 글"))
                .andExpect(jsonPath("$.content").value("공개 글 본문"))
                .andExpect(jsonPath("$.categoryName").value("테크"))
                .andExpect(jsonPath("$.hashTags.length()").value(2));
    }

    @Test
    void 익명_사용자는_비공개_블로그_상세를_조회할_수_없다() throws Exception {
        mockMvc.perform(get("/api/v1/blog/posts/{postId}", PRIVATE_POST_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_비공개_블로그_상세를_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/blog/posts/{postId}", PRIVATE_POST_ID)
                        .header(MEMBER_ID, ADMIN_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("비공개 글"));
    }

    @Test
    void 발행되지_않은_블로그_상세는_404를_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/blog/posts/{postId}", DRAFT_POST_ID)
                        .header(MEMBER_ID, ADMIN_MEMBER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/blog/additional-category.json")
    void 블로그_카테고리_목록을_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/blog/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("테크"))
                .andExpect(jsonPath("$[1].categoryName").value("라이프"))
                .andExpect(jsonPath("$[1].id").value(2));
    }
}
