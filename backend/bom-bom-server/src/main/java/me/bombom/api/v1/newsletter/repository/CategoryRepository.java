package me.bombom.api.v1.newsletter.repository;

import me.bombom.api.v1.newsletter.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String category);
}
