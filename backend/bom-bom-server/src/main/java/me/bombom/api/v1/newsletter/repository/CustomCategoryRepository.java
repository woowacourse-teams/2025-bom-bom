package me.bombom.api.v1.newsletter.repository;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.newsletter.dto.CategoryResponse;

public interface CustomCategoryRepository {

    List<CategoryResponse> findCategories(boolean includeSuspended, LocalDate thresholdDate);
}
