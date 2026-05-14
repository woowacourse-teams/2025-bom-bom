package me.bombom.api.v1.notice.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.notice.dto.NoticeResponse;
import me.bombom.api.v1.notice.service.NoticeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notices")
public class NoticeController implements NoticeControllerApi {

    private final NoticeService noticeService;

    @Override
    @GetMapping
    public Page<NoticeResponse> getNotices(
            @PageableDefault(size = 20)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "id", direction = Sort.Direction.ASC)
            }) Pageable pageable
    ) {
        return noticeService.getNotices(pageable);
    }
}
