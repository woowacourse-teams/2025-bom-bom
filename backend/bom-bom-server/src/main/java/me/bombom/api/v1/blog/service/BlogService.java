package me.bombom.api.v1.blog.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.dto.response.BlogPostResponse;
import me.bombom.api.v1.blog.repository.BlogPostRepository;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogService {

    private final BlogPostRepository blogPostRepository;

    public Page<BlogPostResponse> getPublishedPosts(Member member, Pageable pageable) {
        Long memberId = member == null ? null : member.getId();
        return blogPostRepository.findPublishedPosts(memberId, pageable);
    }
}
