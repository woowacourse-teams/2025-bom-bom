package me.bombom.api.v1.article.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record DeleteArticlesRequest(@NotEmpty List<Long> articleIds) {
}
