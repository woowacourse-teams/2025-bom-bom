package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailUserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailUserAnswerRepository extends JpaRepository<MaeilMailUserAnswer, Long> {
}
