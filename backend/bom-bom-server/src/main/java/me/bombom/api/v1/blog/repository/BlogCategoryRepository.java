package me.bombom.api.v1.blog.repository;

import java.util.List;
import me.bombom.api.v1.blog.domain.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Long> {

    List<BlogCategory> findAllByOrderByIdAsc();
}
