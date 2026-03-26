package me.bombom.api.v1.blog.repository;

import me.bombom.api.v1.blog.domain.BlogImageAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogImageAssetRepository extends JpaRepository<BlogImageAsset, Long> {
}
