package me.bombom.api.v1.notice.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.notice.dto.NoticeResponse;
import me.bombom.api.v1.notice.service.NoticeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notices")
public class NoticeController implements NoticeControllerApi {

    private final NoticeService noticeService;

    @Override
    @GetMapping
    public List<NoticeResponse> getNotices() {
        return noticeService.getNotices();
    }
}
