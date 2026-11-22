package me.bombom.api.v1.signupsource.repository;

import java.util.List;
import me.bombom.api.v1.signupsource.domain.SignupRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignupRecommendationRepository extends JpaRepository<SignupRecommendation, Long> {

    List<SignupRecommendation> findAllByOrderByDisplayOrderAsc();
}
