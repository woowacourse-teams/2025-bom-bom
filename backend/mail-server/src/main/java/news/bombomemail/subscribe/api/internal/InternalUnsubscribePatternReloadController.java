package news.bombomemail.subscribe.api.internal;

import lombok.RequiredArgsConstructor;
import news.bombomemail.subscribe.service.UnsubscribePatternReloadService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/unsubscribe-patterns")
@RequiredArgsConstructor
public class InternalUnsubscribePatternReloadController {

    private final UnsubscribePatternReloadService unsubscribePatternReloadService;

    @PostMapping("/reload")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reload() {
        unsubscribePatternReloadService.reload();
    }
}
