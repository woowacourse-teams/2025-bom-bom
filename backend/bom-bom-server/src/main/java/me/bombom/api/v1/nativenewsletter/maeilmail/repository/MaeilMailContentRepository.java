package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailContentRepository extends JpaRepository<MaeilMailContent, Long> {
}
