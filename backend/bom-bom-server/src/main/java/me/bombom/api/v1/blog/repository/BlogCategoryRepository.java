package me.bombom.api.v1.blog.repository;

import me.bombom.api.v1.blog.domain.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Long> {
}
