package me.bombom.api.v1.blog.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.blog.domain.BlogCategory;
import me.bombom.api.v1.blog.domain.BlogImageAsset;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogPostVisibility;
import me.bombom.api.v1.blog.dto.response.BlogPostResponse;
import me.bombom.api.v1.blog.repository.BlogCategoryRepository;
import me.bombom.api.v1.blog.repository.BlogImageAssetRepository;
import me.bombom.api.v1.blog.repository.BlogPostRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.Role;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@IntegrationTest
class BlogServiceTest {

    @Autowired
    private BlogService blogService;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogCategoryRepository blogCategoryRepository;

    @Autowired
    private BlogImageAssetRepository blogImageAssetRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Member userMember;
    private Member adminMember;

    @BeforeEach
    void setUp() {
        initializeRoles();
        blogImageAssetRepository.deleteAllInBatch();
        blogPostRepository.deleteAllInBatch();
        blogCategoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        Role userRole = findRoleByAuthority("USER");
        Role adminRole = findRoleByAuthority("ADMIN");

        userMember = memberRepository.save(TestFixture.createMemberWithRole("blog-user", "blog-user", userRole.getId()));
        adminMember = memberRepository.save(TestFixture.createMemberWithRole("blog-admin", "blog-admin", adminRole.getId()));

        BlogCategory category = blogCategoryRepository.save(TestFixture.createBlogCategory("테크"));

        BlogPost publicPost = blogPostRepository.save(TestFixture.createBlogPost(
                userMember.getId(),
                "공개 글",
                null,
                category.getId(),
                BlogPostStatus.PUBLISHED,
                BlogPostVisibility.PUBLIC,
                LocalDateTime.of(2026, 3, 25, 9, 0)
        ));

        BlogImageAsset thumbnail = blogImageAssetRepository.save(
                TestFixture.createBlogImageAsset(publicPost.getId(), "blog/public-thumb", "https://cdn.bombom.me/public.png")
        );
        ReflectionTestUtils.setField(publicPost, "thumbnailImageId", thumbnail.getId());
        blogPostRepository.save(publicPost);

        blogPostRepository.save(TestFixture.createBlogPost(
                adminMember.getId(),
                "비공개 글",
                null,
                category.getId(),
                BlogPostStatus.PUBLISHED,
                BlogPostVisibility.PRIVATE,
                LocalDateTime.of(2026, 3, 24, 9, 0)
        ));

        blogPostRepository.save(TestFixture.createBlogPost(
                userMember.getId(),
                "임시 글",
                null,
                category.getId(),
                BlogPostStatus.DRAFT,
                BlogPostVisibility.PUBLIC,
                LocalDateTime.of(2026, 3, 23, 9, 0)
        ));
    }

    @Test
    void 익명_사용자가_블로그_목록을_조회한다() {
        // when
        Page<BlogPostResponse> result = blogService.getPublishedPosts(
                null,
                PageRequest.of(0, 20, Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id")))
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(1);
            softly.assertThat(result.getContent()).hasSize(1);
            softly.assertThat(result.getContent().getFirst().title()).isEqualTo("공개 글");
            softly.assertThat(result.getContent().getFirst().thumbnailImageUrl()).isEqualTo("https://cdn.bombom.me/public.png");
            softly.assertThat(result.getContent().getFirst().categoryName()).isEqualTo("테크");
        });
    }

    @Test
    void 일반_사용자가_블로그_목록을_조회한다() {
        // when
        Page<BlogPostResponse> result = blogService.getPublishedPosts(
                userMember,
                PageRequest.of(0, 20, Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id")))
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(1);
            softly.assertThat(result.getContent()).hasSize(1);
            softly.assertThat(result.getContent().getFirst().title()).isEqualTo("공개 글");
        });
    }

    @Test
    void 관리자가_블로그_목록을_조회한다() {
        // when
        Page<BlogPostResponse> result = blogService.getPublishedPosts(
                adminMember,
                PageRequest.of(0, 20, Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id")))
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(2);
            softly.assertThat(result.getContent()).hasSize(2);
            softly.assertThat(result.getContent().get(0).title()).isEqualTo("공개 글");
            softly.assertThat(result.getContent().get(1).title()).isEqualTo("비공개 글");
        });
    }

    @Test
    void 블로그_글_목록을_페이지네이션한다() {
        // given
        blogPostRepository.save(TestFixture.createBlogPost(
                userMember.getId(),
                "두번째 공개 글",
                null,
                null,
                BlogPostStatus.PUBLISHED,
                BlogPostVisibility.PUBLIC,
                LocalDateTime.of(2026, 3, 22, 9, 0)
        ));

        // when
        Page<BlogPostResponse> result = blogService.getPublishedPosts(
                null,
                PageRequest.of(0, 1, Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id")))
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(2);
            softly.assertThat(result.getTotalPages()).isEqualTo(2);
            softly.assertThat(result.getContent()).hasSize(1);
            softly.assertThat(result.getContent().getFirst().title()).isEqualTo("공개 글");
            softly.assertThat(result.hasNext()).isTrue();
        });
    }

    private void initializeRoles() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
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
