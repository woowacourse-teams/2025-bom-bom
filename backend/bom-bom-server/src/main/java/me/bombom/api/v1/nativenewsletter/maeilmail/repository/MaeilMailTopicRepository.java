package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import java.util.List;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailTopicRepository extends JpaRepository<MaeilMailTopic, Long> {

    List<MaeilMailTopic> findAllByOrderByDisplayOrderAsc();
}
