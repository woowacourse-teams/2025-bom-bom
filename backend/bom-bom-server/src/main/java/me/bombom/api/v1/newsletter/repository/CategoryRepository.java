package me.bombom.api.v1.newsletter.repository;

import java.util.List;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.dto.CategoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String category);

    @Query("""
            SELECT new me.bombom.api.v1.newsletter.dto.CategoryResponse(c.id, c.name)
            FROM Category c
            WHERE c.id IN :ids
    """)
    List<CategoryResponse> findCategoryInfoByIds(@Param("ids") List<Long> ids);
}
