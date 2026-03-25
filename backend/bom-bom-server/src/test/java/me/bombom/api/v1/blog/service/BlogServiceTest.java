package me.bombom.api.v1.blog.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.blog.domain.BlogCategory;
import me.bombom.api.v1.blog.domain.BlogHashtag;
import me.bombom.api.v1.blog.domain.BlogImageAsset;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogPostVisibility;
import me.bombom.api.v1.blog.dto.response.BlogCategoryResponse;
import me.bombom.api.v1.blog.dto.response.BlogPostDetailResponse;
import me.bombom.api.v1.blog.dto.response.BlogPostResponse;
import me.bombom.api.v1.blog.repository.BlogCategoryRepository;
import me.bombom.api.v1.blog.repository.BlogHashtagRepository;
import me.bombom.api.v1.blog.repository.BlogImageAssetRepository;
import me.bombom.api.v1.blog.repository.BlogPostRepository;
import me.bombom.api.v1.blog.repository.BlogPostTagRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
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
    private BlogHashtagRepository blogHashtagRepository;

    @Autowired
    private BlogPostTagRepository blogPostTagRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Member userMember;
    private Member adminMember;
    private BlogPost publicPost;
    private BlogPost privatePost;
    private BlogPost draftPost;

    @BeforeEach
    void setUp() {
        initializeRoles();
        blogPostTagRepository.deleteAllInBatch();
        blogHashtagRepository.deleteAllInBatch();
        blogImageAssetRepository.deleteAllInBatch();
        blogPostRepository.deleteAllInBatch();
        blogCategoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        Role userRole = findRoleByAuthority("USER");
        Role adminRole = findRoleByAuthority("ADMIN");

        userMember = memberRepository.save(TestFixture.createMemberWithRole("blog-user", "blog-user", userRole.getId()));
        adminMember = memberRepository.save(TestFixture.createMemberWithRole("blog-admin", "blog-admin", adminRole.getId()));

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
                TestFixture.createBlogImageAsset(publicPost.getId(), "blog/public-thumb", "https://cdn.bombom.me/public.png")
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
                "두번째 공개 글 본문",
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

    @Test
    void 익명_사용자가_공개_블로그_상세를_조회한다() {
        // when
        BlogPostDetailResponse result = blogService.getPublishedPostDetail(publicPost.getId(), null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.title()).isEqualTo("공개 글");
            softly.assertThat(result.content()).isEqualTo("공개 글 본문");
            softly.assertThat(result.thumbnailImageUrl()).isEqualTo("https://cdn.bombom.me/public.png");
            softly.assertThat(result.categoryName()).isEqualTo("테크");
            softly.assertThat(result.publishedAt()).isEqualTo(LocalDateTime.of(2026, 3, 25, 9, 0));
            softly.assertThat(result.hashTags()).containsExactlyInAnyOrder("스프링", "백엔드");
        });
    }

    @Test
    void 일반_사용자가_비공개_블로그_상세를_조회하면_예외가_발생한다() {
        assertThatThrownBy(() -> blogService.getPublishedPostDetail(privatePost.getId(), userMember))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void 관리자가_비공개_블로그_상세를_조회한다() {
        // when
        BlogPostDetailResponse result = blogService.getPublishedPostDetail(privatePost.getId(), adminMember);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.title()).isEqualTo("비공개 글");
            softly.assertThat(result.content()).isEqualTo("비공개 글 본문");
            softly.assertThat(result.thumbnailImageUrl()).isNull();
            softly.assertThat(result.categoryName()).isEqualTo("테크");
            softly.assertThat(result.hashTags()).isEmpty();
        });
    }

    @Test
    void 발행되지_않은_블로그_글은_상세_조회할_수_없다() {
        assertThatThrownBy(() -> blogService.getPublishedPostDetail(draftPost.getId(), adminMember))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 블로그_카테고리_목록을_조회한다() {
        // given
        BlogCategory secondCategory = blogCategoryRepository.save(TestFixture.createBlogCategory("라이프"));

        // when
        List<BlogCategoryResponse> result = blogService.getBlogCategories();

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result.get(0).id()).isNotNull();
            softly.assertThat(result.get(0).categoryName()).isEqualTo("테크");
            softly.assertThat(result.get(1).id()).isEqualTo(secondCategory.getId());
            softly.assertThat(result.get(1).categoryName()).isEqualTo("라이프");
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
