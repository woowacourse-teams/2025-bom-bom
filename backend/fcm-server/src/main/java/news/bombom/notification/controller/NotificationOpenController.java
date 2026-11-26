package news.bombom.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import news.bombom.notification.dto.request.NotificationOpenRequest;
import news.bombom.notification.service.NotificationOpenService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationOpenController {

    private final NotificationOpenService notificationOpenService;

    @PostMapping("/opens")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordOpen(@Valid @RequestBody NotificationOpenRequest request) {
        notificationOpenService.recordOpen(request);
    }
}
