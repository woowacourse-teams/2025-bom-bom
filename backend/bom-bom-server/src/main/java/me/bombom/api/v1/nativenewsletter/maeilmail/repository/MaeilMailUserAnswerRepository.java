package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailUserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaeilMailUserAnswerRepository extends JpaRepository<MaeilMailUserAnswer, Long> {

    Optional<MaeilMailUserAnswer> findByMemberIdAndContentId(Long memberId, Long contentId);
}
