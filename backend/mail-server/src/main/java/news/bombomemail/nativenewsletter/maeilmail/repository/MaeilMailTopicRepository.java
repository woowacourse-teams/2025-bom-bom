package news.bombomemail.nativenewsletter.maeilmail.repository;

import java.util.List;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailTopicRepository extends JpaRepository<MaeilMailTopic, Long> {

    List<MaeilMailTopic> findAllByOrderByDisplayOrderAsc();
}
