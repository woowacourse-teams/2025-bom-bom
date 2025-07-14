package me.bombom.api.v1.newsletter.repository;

import java.util.List;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {

    List<Newsletter> findAllByOrderBySubscribeCountDescNameAsc();
}
