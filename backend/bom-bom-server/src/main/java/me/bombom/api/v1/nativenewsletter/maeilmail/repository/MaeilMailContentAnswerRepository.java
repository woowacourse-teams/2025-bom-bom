package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaeilMailContentAnswerRepository extends JpaRepository<MaeilMailContentAnswer, Long> {

    Optional<MaeilMailContentAnswer> findByContentId(Long contentId);
}
