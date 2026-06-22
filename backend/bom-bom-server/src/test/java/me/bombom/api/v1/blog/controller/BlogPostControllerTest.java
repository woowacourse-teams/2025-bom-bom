package me.bombom.api.v1.blog.controller;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
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
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.Role;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@IntegrationTest
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

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Member adminMember;
    private BlogPost publicPost;
    private BlogPost privatePost;
    private BlogPost draftPost;

    @BeforeEach
    void setUp() {
        initializeRoles();

        Role userRole = findRoleByAuthority("USER");
        Role adminRole = findRoleByAuthority("ADMIN");
        Member userMember = memberRepository.save(
                TestFixture.createMemberWithRole("blog-user", "blog-user", userRole.getId())
        );
        adminMember = memberRepository.save(
                TestFixture.createMemberWithRole("blog-admin", "blog-admin", adminRole.getId())
        );
        BlogCategory category = blogCategoryRepository.save(TestFixture.createBlogCategory("테크"));

        publicPost = blogPostRepository.save(TestFixture.createBlogPost(
                userMember.getId(),
                "공개 글",
                "공개 글 본문",
                null,
                category.getId(),
                BlogPostStatus.PUBLISHED,
                BlogPostVisibility.PUBLIC,
                LocalDateTime.of(2026, 3, 25, 9, 0)
        ));
        BlogImageAsset thumbnail = blogImageAssetRepository.save(
                TestFixture.createBlogImageAsset(
                        publicPost.getId(),
                        "blog/public-thumb",
                        "https://cdn.bombom.me/public.png"
                )
        );
        ReflectionTestUtils.setField(publicPost, "thumbnailImageId", thumbnail.getId());
        blogPostRepository.save(publicPost);

        BlogHashtag firstHashtag = blogHashtagRepository.save(TestFixture.createBlogHashtag("스프링"));
        BlogHashtag secondHashtag = blogHashtagRepository.save(TestFixture.createBlogHashtag("백엔드"));
        blogPostTagRepository.save(TestFixture.createBlogPostTag(publicPost.getId(), firstHashtag.getId()));
        blogPostTagRepository.save(TestFixture.createBlogPostTag(publicPost.getId(), secondHashtag.getId()));

        privatePost = blogPostRepository.save(TestFixture.createBlogPost(
                adminMember.getId(),
                "비공개 글",
                "비공개 글 본문",
                null,
                category.getId(),
                BlogPostStatus.PUBLISHED,
                BlogPostVisibility.PRIVATE,
                LocalDateTime.of(2026, 3, 24, 9, 0)
        ));
        draftPost = blogPostRepository.save(TestFixture.createBlogPost(
                userMember.getId(),
                "임시 글",
                "임시 글 본문",
                null,
                category.getId(),
                BlogPostStatus.DRAFT,
                BlogPostVisibility.PUBLIC,
                LocalDateTime.of(2026, 3, 23, 9, 0)
        ));
    }

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
        mockMvc.perform(get("/api/v1/blog/posts")
                        .header(AcceptanceTestHeaders.MEMBER_ID, adminMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value("공개 글"))
                .andExpect(jsonPath("$.content[1].title").value("비공개 글"));
    }

    @Test
    void 블로그_목록에_페이징을_적용한다() throws Exception {
        blogPostRepository.save(TestFixture.createBlogPost(
                adminMember.getId(),
                "두 번째 공개 글",
                "본문",
                null,
                null,
                BlogPostStatus.PUBLISHED,
                BlogPostVisibility.PUBLIC,
                LocalDateTime.of(2026, 3, 22, 9, 0)
        ));

        mockMvc.perform(get("/api/v1/blog/posts").queryParam("page", "0").queryParam("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void 익명_사용자가_공개_블로그_상세를_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/blog/posts/{postId}", publicPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("공개 글"))
                .andExpect(jsonPath("$.content").value("공개 글 본문"))
                .andExpect(jsonPath("$.categoryName").value("테크"))
                .andExpect(jsonPath("$.hashTags.length()").value(2));
    }

    @Test
    void 익명_사용자는_비공개_블로그_상세를_조회할_수_없다() throws Exception {
        mockMvc.perform(get("/api/v1/blog/posts/{postId}", privatePost.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_비공개_블로그_상세를_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/blog/posts/{postId}", privatePost.getId())
                        .header(AcceptanceTestHeaders.MEMBER_ID, adminMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("비공개 글"));
    }

    @Test
    void 발행되지_않은_블로그_상세는_404를_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/blog/posts/{postId}", draftPost.getId())
                        .header(AcceptanceTestHeaders.MEMBER_ID, adminMember.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void 블로그_카테고리_목록을_조회한다() throws Exception {
        BlogCategory secondCategory = blogCategoryRepository.save(TestFixture.createBlogCategory("라이프"));

        String response = mockMvc.perform(get("/api/v1/blog/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("테크"))
                .andExpect(jsonPath("$[1].categoryName").value("라이프"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertSoftly(softly -> {
            softly.assertThat(response).contains(secondCategory.getId().toString());
        });
    }

    private void initializeRoles() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.executeWithoutResult(status -> {
            entityManager.createNativeQuery("TRUNCATE TABLE role").executeUpdate();
            entityManager.persist(Role.builder().authority("USER").build());
            entityManager.persist(Role.builder().authority("ADMIN").build());
            entityManager.flush();
        });
    }

    private Role findRoleByAuthority(String authority) {
        return entityManager.createQuery(
                        "SELECT r FROM Role r WHERE r.authority = :authority",
                        Role.class
                )
                .setParameter("authority", authority)
                .getSingleResult();
    }
}
