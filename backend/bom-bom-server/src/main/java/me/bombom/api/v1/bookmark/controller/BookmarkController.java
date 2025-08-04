package me.bombom.api.v1.bookmark.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.bookmark.dto.BookmarkResponse;
import me.bombom.api.v1.bookmark.service.BookmarkService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping
    public Page<BookmarkResponse> getBookmarks(
            @LoginMember Member member,
            @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
    ) {
        return bookmarkService.getBookmarks(member.getId(), pageable);
    }
}
