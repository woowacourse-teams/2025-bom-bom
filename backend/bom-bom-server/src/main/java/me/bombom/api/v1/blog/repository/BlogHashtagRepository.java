package me.bombom.api.v1.blog.repository;

import me.bombom.api.v1.blog.domain.BlogHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogHashtagRepository extends JpaRepository<BlogHashtag, Long> {
}
